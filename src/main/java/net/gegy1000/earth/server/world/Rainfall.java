package net.gegy1000.earth.server.world;

public final class Rainfall {
    // TODO: These values?
    public static boolean isWet(float annualRainfall) {
        return annualRainfall > 2400;
    }

    public static boolean isDry(float annualRainfall) {
        return annualRainfall < 380;
    }
}
