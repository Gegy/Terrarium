package net.gegy1000.earth.server.world.ecology;

public interface GrowthIndicator {
    static GrowthIndicator relaxed() {
        return predictors -> 1.0;
    }

    double evaluate(GrowthPredictors predictors);
}
