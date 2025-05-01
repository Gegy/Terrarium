package dev.gegy.terrarium.classifier;

import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import dev.gegy.terrarium.backend.expr.classifier.Classifiers;
import dev.gegy.terrarium.backend.expr.predictor.Predictor;
import dev.gegy.terrarium.backend.expr.predictor.Predictors;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ClassifierTrainer<T, R> {
    private final List<Predictor<T>> features;
    private final int maxDepth;
    private final int minSamples;
    private final Executor executor;

    public ClassifierTrainer(final List<Predictor<T>> features, final int maxDepth, final int minSamples, final Executor executor) {
        this.features = features;
        this.maxDepth = maxDepth;
        this.minSamples = minSamples;
        this.executor = executor;
    }

    public CompletableFuture<ClassifierNode<T, R>> build(final List<Sample<T, R>> samples) {
        if (samples.isEmpty()) {
            throw new IllegalArgumentException("Cannot build classifier without any sample points");
        }
        return buildNode(buildSplitNode(samples), 0);
    }

    private CompletableFuture<ClassifierNode<T, R>> buildNode(final SplitNode<T, R> node, final int depth) {
        if (depth > 0 && node.samples.size() < minSamples || depth > maxDepth) {
            return CompletableFuture.completedFuture(Classifiers.leaf(node.bestChoice));
        }
        return CompletableFuture.supplyAsync(() -> chooseSplit(node), executor).thenCompose(split -> {
            if (split == null) {
                return CompletableFuture.completedFuture(Classifiers.leaf(node.bestChoice));
            }
            final CompletableFuture<ClassifierNode<T, R>> leftFuture = buildNode(split.left, depth + 1);
            final CompletableFuture<ClassifierNode<T, R>> rightFuture = buildNode(split.right, depth + 1);
            return leftFuture.thenCombine(rightFuture, (left, right) -> {
                if (left.equals(right)) {
                    return left;
                }
                return Classifiers.threshold(
                        Predictors.opaque(split.feature),
                        Predictors.constant(split.threshold),
                        left,
                        right
                );
            });
        });
    }

    @Nullable
    private Split<T, R> chooseSplit(final SplitNode<T, R> parent) {
        final float rootImpurity = parent.giniImpurity;
        if (rootImpurity <= 0.01f) {
            return null;
        }
        return parent.samples.parallelStream()
                .<Split<T, R>>mapMulti((sample, output) -> {
                    for (final Predictor<T> feature : features) {
                        final float threshold = feature.evaluate(sample.features);
                        final Split<T, R> candidateSplit = split(parent.samples, feature, threshold);
                        if (candidateSplit != null && candidateSplit.giniImpurity() < rootImpurity) {
                            output.accept(candidateSplit);
                        }
                    }
                })
                .min(Comparator.comparingDouble(Split::giniImpurity))
                .orElse(null);
    }

    private static <R> float computeGiniImpurity(final Object2IntOpenHashMap<R> counts, final int totalCount) {
        float impurity = 1.0f;
        for (final Object2IntMap.Entry<R> entry : counts.object2IntEntrySet()) {
            final int count = entry.getIntValue();
            final float probability = (float) count / totalCount;
            impurity -= probability * probability;
        }
        return impurity;
    }

    @Nullable
    private static <T, R> Split<T, R> split(final List<Sample<T, R>> samples, final Predictor<T> feature, final float threshold) {
        final List<Sample<T, R>> left = new ArrayList<>(samples.size() * 3 / 2);
        final List<Sample<T, R>> right = new ArrayList<>(samples.size() * 3 / 2);
        for (final Sample<T, R> sample : samples) {
            if (feature.evaluate(sample.features) >= threshold) {
                left.add(sample);
            } else {
                right.add(sample);
            }
        }
        if (left.isEmpty() || right.isEmpty()) {
            return null;
        }
        return new Split<>(buildSplitNode(left), buildSplitNode(right), feature, threshold);
    }

    private static <T, R> SplitNode<T, R> buildSplitNode(final List<Sample<T, R>> samples) {
        final Object2IntOpenHashMap<R> counts = new Object2IntOpenHashMap<>();
        R bestChoice = null;
        int bestCount = 0;
        for (final Sample<T, R> sample : samples) {
            final int count = counts.addTo(sample.choice, 1) + 1;
            if (count > bestCount) {
                bestChoice = sample.choice;
                bestCount = count;
            }
        }
        if (bestChoice == null) {
            throw new IllegalStateException("Found no best choice for split");
        }
        final float giniImpurity = computeGiniImpurity(counts, samples.size());
        return new SplitNode<>(samples, bestChoice, giniImpurity);
    }

    private record Split<T, R>(SplitNode<T, R> left, SplitNode<T, R> right, Predictor<T> feature, float threshold) {
        public float giniImpurity() {
            final int totalCount = left.samples.size() + right.samples.size();
            return left.giniImpurity * left.samples.size() / totalCount
                    + right.giniImpurity * right.samples.size() / totalCount;
        }
    }

    private record SplitNode<T, R>(List<Sample<T, R>> samples, R bestChoice, float giniImpurity) {
    }

    public record Sample<T, R>(T features, R choice) {
    }
}
