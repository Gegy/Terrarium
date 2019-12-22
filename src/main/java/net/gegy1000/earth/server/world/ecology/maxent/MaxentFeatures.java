package net.gegy1000.earth.server.world.ecology.maxent;

import net.gegy1000.earth.server.world.ecology.GrowthIndicator;

public final class MaxentFeatures {
    public static GrowthIndicator raw(GrowthIndicator feature, double lambda, double min, double max) {
        return predictors -> {
            // TODO: These math ops could be optimized, but for clarity, keeping them as is unless it causes a problem
            double value = feature.evaluate(predictors);
            return lambda * (value - min) / (max - min);
        };
    }

    public static GrowthIndicator quadratic(GrowthIndicator feature, double lambda, double min, double max) {
        return predictors -> {
            double value = feature.evaluate(predictors);
            return lambda * (value * value - min) / (max - min);
        };
    }

    public static GrowthIndicator product(GrowthIndicator a, GrowthIndicator b, double lambda, double min, double max) {
        return predictors -> {
            double valueA = a.evaluate(predictors);
            double valueB = b.evaluate(predictors);
            return lambda * (valueA * valueB - min) / (max - min);
        };
    }

    public static GrowthIndicator hinge(GrowthIndicator feature, double lambda, double hinge, double max) {
        return predictors -> {
            double value = feature.evaluate(predictors);
            if (value <= hinge) return 0.0;
            return lambda * (value - hinge) / (max - hinge);
        };
    }

    public static GrowthIndicator reverseHinge(GrowthIndicator feature, double lambda, double min, double hinge) {
        return predictors -> {
            double value = feature.evaluate(predictors);
            if (value >= hinge) return 0.0;
            return lambda * (hinge - value) / (hinge - min);
        };
    }

    public static GrowthIndicator threshold(GrowthIndicator feature, double lambda, double min, double max, double threshold) {
        return predictors -> {
            double value = feature.evaluate(predictors);
            return lambda * (value >= threshold ? max : min);
        };
    }

    public static GrowthIndicator equal(GrowthIndicator feature, double lambda, double min, double max, double eq) {
        return predictors -> {
            double value = feature.evaluate(predictors);
            return lambda * (Math.abs(value - eq) < 1e-2 ? max : min);
        };
    }
}
