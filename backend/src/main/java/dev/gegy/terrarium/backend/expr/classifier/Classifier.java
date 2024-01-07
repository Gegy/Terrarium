package dev.gegy.terrarium.backend.expr.classifier;

public interface Classifier<T, R> {
    R evaluate(T parameters);
}
