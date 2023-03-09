package dev.gegy.terrarium.backend.maxent;

import dev.gegy.terrarium.backend.expr.predictor.Predictor;
import dev.gegy.terrarium.backend.expr.predictor.PredictorNode;
import dev.gegy.terrarium.backend.expr.predictor.Predictors;

import java.util.List;

import static dev.gegy.terrarium.backend.expr.predictor.Predictors.*;

public interface Feature<T> {
    PredictorNode<T> toPredictor();

    record Const<T>(float value) implements Feature<T> {
        private static final Const<?> ZERO = new Const<>(0.0f);

        @SuppressWarnings("unchecked")
        public static <T> Const<T> zero() {
            return (Const<T>) ZERO;
        }

        @Override
        public PredictorNode<T> toPredictor() {
            return constant(value);
        }
    }

    record Parameter<T>(Predictor<T> parameter) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            return opaque(parameter);
        }
    }

    record Equal<T>(Feature<T> value, float lambda, float min, float max, float eq) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            return ifEqual(
                    value.toPredictor(),
                    constant(eq),
                    constant(max * lambda),
                    constant(min * lambda)
            );
        }
    }

    record Hinge<T>(Feature<T> value, float lambda, float hinge, float max) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            return mul(
                    Predictors.max(
                            sub(value.toPredictor(), constant(hinge)),
                            constant(0.0f)
                    ),
                    constant(lambda / (max - hinge))
            );
        }
    }

    record ReverseHinge<T>(Feature<T> value, float lambda, float min, float hinge) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            return mul(
                    max(
                            sub(constant(hinge), value.toPredictor()),
                            constant(0.0f)
                    ),
                    constant(lambda / (hinge - min))
            );
        }
    }

    record Product<T>(Feature<T> left, Feature<T> right, float lambda, float min, float max) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            return mul(
                    sub(mul(left.toPredictor(), right.toPredictor()), constant(min)),
                    constant(lambda / (max - min))
            );
        }
    }

    record Quadratic<T>(Feature<T> value, float lambda, float min, float max) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            final PredictorNode<T> value = this.value.toPredictor();
            return mul(
                    sub(mul(value, value), constant(min)),
                    constant(lambda / (max - min))
            );
        }
    }

    record Raw<T>(Feature<T> value, float lambda, float min, float max) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            return mul(
                    sub(value.toPredictor(), constant(min)),
                    constant(lambda / (max - min))
            );
        }
    }

    record Threshold<T>(Feature<T> value, float lambda, float min, float max, float threshold) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            return step(
                    value.toPredictor(),
                    constant(threshold),
                    constant(min * lambda),
                    constant(max * lambda)
            );
        }
    }

    record Output<T>(List<Feature<T>> features, float linearPredictorNormalizer, float densityNormalizer, float entropy, MaxentOutputType output) implements Feature<T> {
        @Override
        public PredictorNode<T> toPredictor() {
            final PredictorNode<T> raw = mul(
                    exp(sub(
                            sum(features.stream().map(Feature::toPredictor).toList()),
                            constant(linearPredictorNormalizer)
                    )),
                    constant(1.0f / densityNormalizer)
            );

            return switch (output) {
                case RAW -> raw;
                case LOGISTIC -> sub(
                        constant(1.0f),
                        div(
                                constant(1.0f),
                                add(
                                        mul(raw, constant((float) Math.exp(entropy))),
                                        constant(1.0f)
                                )
                        )
                );
                case CLOGLOG -> sub(
                        constant(1.0f),
                        exp(mul(raw, constant((float) -Math.exp(entropy))))
                );
            };
        }
    }
}
