package net.gegy1000.earth.server.world;

public final class Temperature {
    public static final float MIN_FREEZE = -14.0F;
    public static final float MEAN_COLD = 8.0F;

    public static boolean isFrozen(float minTemperature, float meanTemperature) {
        return (minTemperature < MIN_FREEZE && meanTemperature < 5.0F);
    }

    public static boolean isCold(float meanTemperature) {
        return meanTemperature < MEAN_COLD;
    }

    public static boolean isWarm(float meanTemperature) {
        return meanTemperature > 18;
    }

    public static boolean isHot(float meanTemperature) {
        return meanTemperature > 22;
    }
}
