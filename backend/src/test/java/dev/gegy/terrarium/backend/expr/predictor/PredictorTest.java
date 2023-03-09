package dev.gegy.terrarium.backend.expr.predictor;

import org.junit.jupiter.api.Test;

import static dev.gegy.terrarium.backend.expr.predictor.Predictors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PredictorTest {
    private record FloatPair(float a, float b) {
    }

    private static float eval(final PredictorNode<Void> node) {
        return PredictorNode.compile(node).evaluate(null);
    }

    private static float eval(final PredictorNode<Float> node, final float value) {
        return PredictorNode.compile(node).evaluate(value);
    }

    private static float eval(final PredictorNode<FloatPair> node, final float a, final float b) {
        return PredictorNode.compile(node).evaluate(new FloatPair(a, b));
    }

    private static PredictorNode<Float> parameter() {
        return opaque(v -> v);
    }

    @Test
    public void evalConstants() {
        assertEquals(1.0f, eval(constant(1.0f)));
        assertEquals(2.0f, eval(add(constant(0.5f), constant(1.5f))));
    }

    @Test
    public void evalAdd() {
        final PredictorNode<FloatPair> sum = add(opaque(FloatPair::a), opaque(FloatPair::b));
        assertEquals(0.0f, eval(sum, 0.0f, 0.0f));
        assertEquals(1.5f, eval(sum, 0.5f, 1.0f));
        assertEquals(3.0f, eval(sum, 2.0f, 1.0f));
    }

    @Test
    public void evalMul() {
        final PredictorNode<FloatPair> product = mul(opaque(FloatPair::a), opaque(FloatPair::b));
        assertEquals(0.0f, eval(product, 0.0f, 0.0f));
        assertEquals(1.5f, eval(product, 0.5f, 3.0f));
        assertEquals(6.0f, eval(product, 2.0f, 3.0f));
    }

    @Test
    public void evalDiv() {
        final PredictorNode<FloatPair> product = div(opaque(FloatPair::a), opaque(FloatPair::b));
        assertEquals(Float.NaN, eval(product, 0.0f, 0.0f));
        assertEquals(1.0f / 3.0f, eval(product, 1.0f, 3.0f));
        assertEquals(4.0f, eval(product, 2.0f, 0.5f));
    }

    @Test
    public void evalEq() {
        final PredictorNode<Float> if3 = ifEqual(parameter(), constant(3.0f), constant(1.0f), constant(0.0f));
        assertEquals(0.0f, eval(if3, 0.0f));
        assertEquals(0.0f, eval(if3, 1.0f));
        assertEquals(0.0f, eval(if3, -3.0f));
        assertEquals(1.0f, eval(if3, 3.0f));
    }

    @Test
    public void evalExp() {
        final PredictorNode<Float> exp = exp(parameter());
        assertEquals(1.0f, eval(exp, 0.0f));
        assertEquals((float) Math.exp(1.0f), eval(exp, 1.0f));
        assertEquals((float) Math.exp(3.0f), eval(exp, 3.0f));
    }

    @Test
    public void evalMax() {
        final PredictorNode<FloatPair> max = max(opaque(FloatPair::a), opaque(FloatPair::b));
        assertEquals(0.0f, eval(max, 0.0f, 0.0f));
        assertEquals(3.0f, eval(max, 1.0f, 3.0f));
        assertEquals(2.0f, eval(max, 2.0f, 0.5f));
    }

    @Test
    public void evalStep() {
        final PredictorNode<Float> step = step(parameter(), constant(2.0f), constant(0.0f), constant(1.0f));
        assertEquals(0.0f, eval(step, 0.0f));
        assertEquals(0.0f, eval(step, 1.0f));
        assertEquals(1.0f, eval(step, 2.0f));
        assertEquals(1.0f, eval(step, 3.0f));
    }

    @Test
    public void evalSub() {
        final PredictorNode<FloatPair> diff = sub(opaque(FloatPair::a), opaque(FloatPair::b));
        assertEquals(0.1f, eval(diff, 0.1f, 0.0f));
        assertEquals(0.5f, eval(diff, 1.0f, 0.5f));
        assertEquals(1.0f, eval(diff, 2.0f, 1.0f));
    }
}
