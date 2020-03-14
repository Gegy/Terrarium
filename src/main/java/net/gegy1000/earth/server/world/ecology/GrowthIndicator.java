package net.gegy1000.earth.server.world.ecology;

public interface GrowthIndicator {
    static GrowthIndicator no() {
        return predictors -> 0.0;
    }

    default GrowthIndicator mul(double scale) {
        return predictors -> this.evaluate(predictors) * scale;
    }

    default GrowthIndicator pow(double pow) {
        return predictors -> Math.pow(this.evaluate(predictors), pow);
    }

    double evaluate(GrowthPredictors predictors);
}
