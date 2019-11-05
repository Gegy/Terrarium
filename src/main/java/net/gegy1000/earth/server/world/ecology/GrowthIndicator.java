package net.gegy1000.earth.server.world.ecology;

public interface GrowthIndicator {
    static GrowthIndicator relaxed() {
        return predictors -> 1.0;
    }

    default GrowthIndicator mul(double scale) {
        return predictors -> this.evaluate(predictors) * scale;
    }

    double evaluate(GrowthPredictors predictors);
}
