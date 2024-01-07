package dev.gegy.terrarium.backend.expr.classifier;

import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;

public class Classifiers {
    private Classifiers() {
    }

    public static <T, R> ClassifierNode<T, R> leaf(final R value) {
        return new ConstClassifier<>(value);
    }

    public static <T, R> ClassifierNode<T, R> threshold(final PredictorNode<T> value, final PredictorNode<T> threshold, final ClassifierNode<T, R> ifGreater, final ClassifierNode<T, R> ifLess) {
        return new ThresholdClassifier<>(value, threshold, ifGreater, ifLess);
    }
}
