package dev.gegy.terrarium.backend.expr.predictor;

public interface Predictor<T> {
    float evaluate(T parameters);
}
