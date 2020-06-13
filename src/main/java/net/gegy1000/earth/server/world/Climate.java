package net.gegy1000.earth.server.world;

public final class Climate {
    public static final float MIN_FREEZE = -14.0F;
    public static final float MEAN_COLD = 5.0F;

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

    // TODO: These values?
    public static boolean isWet(float annualRainfall) {
        return annualRainfall > 2400;
    }

    public static boolean isVeryDry(float annualRainfall) {
        return annualRainfall < 380;
    }

    public static boolean isDry(float annualRainfall) {
        return annualRainfall < 508;
    }

    public static boolean isDesert(float annualRainfall) {
        return annualRainfall < 250;
    }

    public static boolean isRainforest(float annualRainfall) {
        return annualRainfall >= 1800;
    }

    public static boolean isTropicalRainforest(float annualRainfall, float meanTemperature) {
        return Climate.isRainforest(annualRainfall) && meanTemperature >= 20;
    }

    public static boolean isTaiga(float annualRainfall, float meanTemperature) {
        return annualRainfall >= 380 && meanTemperature >= -30 && meanTemperature <= 10;
    }
}
