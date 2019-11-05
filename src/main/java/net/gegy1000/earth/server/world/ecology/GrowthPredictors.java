package net.gegy1000.earth.server.world.ecology;

import net.gegy1000.earth.TerrariumEarth;

public final class GrowthPredictors {
    public float elevation;
    public float annualRainfall;
    public float averageTemperature;

    public static GrowthIndicator byId(String id) {
        switch (id) {
            case "elevation":
                return p -> p.elevation;
            case "annualRainfall":
                return p -> p.annualRainfall;
            case "averageTemperature":
                return p -> p.averageTemperature;
            default:
                TerrariumEarth.LOGGER.warn("invalid predictor id: {}", id);
                return p -> 0.0;
        }
    }
}
