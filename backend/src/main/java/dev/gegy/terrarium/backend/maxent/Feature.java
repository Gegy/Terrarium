package dev.gegy.terrarium.backend.maxent;

import java.util.List;

public interface Feature<T> {
    record Const<T>(float value) implements Feature<T> {
        private static final Const<?> ZERO = new Const<>(0.0f);

        @SuppressWarnings("unchecked")
        public static <T> Const<T> zero() {
            return (Const<T>) ZERO;
        }
    }

    record Parameter<T>(String parameter) implements Feature<T> {
    }

    record Equal<T>(Feature<T> value, float lambda, float min, float max, float eq) implements Feature<T> {
    }

    record Hinge<T>(Feature<T> value, float lambda, float hinge, float max) implements Feature<T> {
    }

    record ReverseHinge<T>(Feature<T> value, float lambda, float min, float hinge) implements Feature<T> {
    }

    record Product<T>(Feature<T> left, Feature<T> right, float lambda, float min, float max) implements Feature<T> {
    }

    record Quadratic<T>(Feature<T> value, float lambda, float min, float max) implements Feature<T> {
    }

    record Raw<T>(Feature<T> value, float lambda, float min, float max) implements Feature<T> {
    }

    record Threshold<T>(Feature<T> value, float lambda, float min, float max, float threshold) implements Feature<T> {
    }

    record Output<T>(List<Feature<T>> features, float linearPredictorNormalizer, float densityNormalizer, float entropy, MaxentOutputType output) implements Feature<T> {
    }
}
