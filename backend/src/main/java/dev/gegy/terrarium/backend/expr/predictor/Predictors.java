package dev.gegy.terrarium.backend.expr.predictor;

import java.util.List;

public class Predictors {
    private Predictors() {
    }

    public static <T> PredictorNode<T> constant(final float value) {
        if (value == 0.0f) {
            return ConstPredictor.zero();
        } else if (value == 1.0f) {
            return ConstPredictor.one();
        }
        return new ConstPredictor<>(value);
    }

    public static <T> PredictorNode<T> opaque(final Predictor<T> parameter) {
        return new OpaquePredictor<>(parameter);
    }

    public static <T> PredictorNode<T> sum(final List<PredictorNode<T>> terms) {
        return switch (terms.size()) {
            case 0 -> ConstPredictor.zero();
            case 1 -> terms.get(0);
            default -> {
                PredictorNode<T> result = add(terms.get(0), terms.get(1));
                for (int i = 2; i < terms.size(); i++) {
                    result = add(result, terms.get(i));
                }
                yield result;
            }
        };
    }

    public static <T> PredictorNode<T> add(final PredictorNode<T> left, final PredictorNode<T> right) {
        if (left.equals(ConstPredictor.zero())) {
            return right;
        } else if (right.equals(ConstPredictor.zero())) {
            return left;
        }
        return new AddPredictor<>(left, right);
    }

    public static <T> PredictorNode<T> sub(final PredictorNode<T> left, final PredictorNode<T> right) {
        if (left.equals(right)) {
            return ConstPredictor.zero();
        } else if (right.equals(ConstPredictor.zero())) {
            return left;
        }
        return new SubPredictor<>(left, right);
    }

    public static <T> PredictorNode<T> mul(final PredictorNode<T> left, final PredictorNode<T> right) {
        if (left.equals(ConstPredictor.zero()) || right.equals(ConstPredictor.zero())) {
            return ConstPredictor.zero();
        } else if (left.equals(ConstPredictor.one())) {
            return right;
        } else if (right.equals(ConstPredictor.one())) {
            return left;
        }
        return new MulPredictor<>(left, right);
    }

    public static <T> PredictorNode<T> div(final PredictorNode<T> left, final PredictorNode<T> right) {
        if (left.equals(ConstPredictor.zero())) {
            return ConstPredictor.zero();
        } else if (right.equals(ConstPredictor.one())) {
            return left;
        }
        return new DivPredictor<>(left, right);
    }

    public static <T> PredictorNode<T> max(final PredictorNode<T> left, final PredictorNode<T> right) {
        if (left.equals(right)) {
            return left;
        }
        return new MaxPredictor<>(left, right);
    }

    public static <T> PredictorNode<T> ifEqual(final PredictorNode<T> left, final PredictorNode<T> right, final PredictorNode<T> ifEqual, final PredictorNode<T> ifNotEqual) {
        if (left.equals(right) || ifEqual.equals(ifNotEqual)) {
            return ifEqual;
        }
        return new EqPredictor<>(left, right, ifEqual, ifNotEqual);
    }

    public static <T> PredictorNode<T> step(final PredictorNode<T> value, final PredictorNode<T> edge, final PredictorNode<T> min, final PredictorNode<T> max) {
        if (value.equals(edge) || max.equals(min)) {
            return max;
        }
        return new StepPredictor<>(value, edge, min, max);
    }

    public static <T> PredictorNode<T> exp(final PredictorNode<T> value) {
        return new ExpPredictor<>(value);
    }
}
