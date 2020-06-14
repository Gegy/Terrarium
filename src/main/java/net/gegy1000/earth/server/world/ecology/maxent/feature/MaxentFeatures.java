package net.gegy1000.earth.server.world.ecology.maxent.feature;

public final class MaxentFeatures {
    public static MaxentFeature raw(MaxentFeature feature, float lambda, float min, float max) {
        return new RawFeature(feature, lambda, min, max);
    }

    public static MaxentFeature quadratic(MaxentFeature feature, float lambda, float min, float max) {
        return new QuadraticFeature(feature, lambda, min, max);
    }

    public static MaxentFeature product(MaxentFeature left, MaxentFeature right, float lambda, float min, float max) {
        return new ProductFeature(left, right, lambda, min, max);
    }

    public static MaxentFeature hinge(MaxentFeature feature, float lambda, float hinge, float max) {
        return new HingeFeature(feature, lambda, hinge, max);
    }

    public static MaxentFeature reverseHinge(MaxentFeature feature, float lambda, float min, float hinge) {
        return new ReverseHingeFeature(feature, lambda, min, hinge);
    }

    public static MaxentFeature threshold(MaxentFeature feature, float lambda, float min, float max, float threshold) {
        return new ThresholdFeature(feature, lambda, min, max, threshold);
    }

    public static MaxentFeature equal(MaxentFeature feature, float lambda, float min, float max, float eq) {
        return new EqualFeature(feature, lambda, min, max, eq);
    }
}
