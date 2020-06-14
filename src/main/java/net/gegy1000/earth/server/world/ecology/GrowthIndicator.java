package net.gegy1000.earth.server.world.ecology;

public interface GrowthIndicator {
    static GrowthIndicator no() {
        return predictors -> 0.0F;
    }

    default GrowthIndicator mul(float scale) {
        return predictors -> this.evaluate(predictors) * scale;
    }

    default GrowthIndicator pow(float pow) {
        return predictors -> (float) Math.pow(this.evaluate(predictors), pow);
    }

    float evaluate(GrowthPredictors predictors);
}
