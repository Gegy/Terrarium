package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.vegetation.TreeDecorator;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.earth.server.world.ecology.vegetation.VegetationGenerator;
import net.gegy1000.terrarium.server.util.WeightedPool;

public final class EarthCoverDecoration {
    private static final double SUITABILITY_THRESHOLD = 0.85;

    public final TreeDecorator trees;
    public final float grassPerChunk;

    private EarthCoverDecoration(TreeDecorator trees, float grassPerChunk) {
        this.trees = trees;
        this.grassPerChunk = grassPerChunk;
    }

    public static class Builder {
        private final GrowthPredictors predictors;

        private final WeightedPool<VegetationGenerator> trees = new WeightedPool<>();

        private VegetationGenerator mostSuitableTree;
        private double mostSuitableTreeIndicator;

        private float minTreeDensity = 0.0F;
        private float maxTreeDensity = 0.2F;

        private float grassPerChunk = 0.5F;
        private boolean noGrass;

        public Builder(GrowthPredictors predictors) {
            this.predictors = predictors;
        }

        public Builder addCandidateTree(Vegetation tree) {
            double indicator = tree.getGrowthIndicator().evaluate(this.predictors);
            if (indicator > SUITABILITY_THRESHOLD) {
                this.trees.add(tree.getGenerator(), (float) indicator);
            }

            if (indicator > this.mostSuitableTreeIndicator) {
                this.mostSuitableTree = tree.getGenerator();
                this.mostSuitableTreeIndicator = indicator;
            }

            return this;
        }

        public Builder setTreeDensity(float minDensity, float maxDensity) {
            this.minTreeDensity = minDensity;
            this.maxTreeDensity = maxDensity;
            return this;
        }

        public Builder requestGrassPerChunk(float grassPerChunk) {
            this.grassPerChunk = Math.max(this.grassPerChunk, grassPerChunk);
            return this;
        }

        public Builder noGrass() {
            this.noGrass = true;
            return this;
        }

        public EarthCoverDecoration build() {
            if (this.trees.isEmpty() && this.mostSuitableTree != null) {
                this.trees.add(this.mostSuitableTree, (float) this.mostSuitableTreeIndicator);
            }

            TreeDecorator trees = new TreeDecorator(this.trees);
            trees.setDensity(this.minTreeDensity, this.maxTreeDensity);
            trees.setRadius(Trees.RADIUS);

            float grassPerChunk = this.noGrass ? 0.0F : this.grassPerChunk;
            return new EarthCoverDecoration(trees, grassPerChunk);
        }
    }
}
