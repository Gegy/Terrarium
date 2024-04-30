package dev.gegy.terrarium.backend.expr.classifier;

import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import dev.gegy.terrarium.backend.expr.predictor.Predictors;

public class Classifiers {
    private Classifiers() {
    }

    public static <T, R> ClassifierNode<T, R> leaf(final R value) {
        return new ConstClassifier<>(value);
    }

    public static <T, R> ClassifierNode<T, R> threshold(final PredictorNode<T> value, final PredictorNode<T> threshold, final ClassifierNode<T, R> ifGreater, final ClassifierNode<T, R> ifLess) {
        return new ThresholdClassifier<>(value, threshold, ifGreater, ifLess);
    }

    public static <T, R> ClassifierNode<T, R> ifTrue(final PredictorNode<T> bool, final ClassifierNode<T, R> ifTrue, final ClassifierNode<T, R> ifFalse) {
        return new ThresholdClassifier<>(bool, Predictors.constant(0.5f), ifTrue, ifFalse);
    }
}
