package dev.gegy.terrarium.backend.expr.predictor;

import dev.gegy.terrarium.backend.expr.classifier.ClassifierNode;
import org.junit.jupiter.api.Test;

import static dev.gegy.terrarium.backend.expr.classifier.Classifiers.leaf;
import static dev.gegy.terrarium.backend.expr.classifier.Classifiers.threshold;
import static dev.gegy.terrarium.backend.expr.predictor.Predictors.constant;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassifierTest {
    private static <R> R eval(final ClassifierNode<Float, R> node, final float value) {
        return ClassifierNode.compile(node).evaluate(value);
    }

    private static PredictorNode<Float> parameter() {
        return Predictors.opaque(v -> v);
    }

    @Test
    public void evalConstants() {
        assertEquals(true, eval(leaf(true), 0.0f));
        assertEquals(false, eval(leaf(false), 0.0f));
    }

    @Test
    public void evalThreshold() {
        final ClassifierNode<Float, Boolean> threshold = threshold(
                parameter(), constant(1.0f),
                leaf(true), leaf(false)
        );
        assertEquals(false, eval(threshold, 0.0f));
        assertEquals(false, eval(threshold, 0.5f));
        assertEquals(true, eval(threshold, 1.0f));
        assertEquals(true, eval(threshold, 2.0f));
    }
}
