package net.gegy1000.earth.server.world.ecology;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

public final class GrowthPredictors {
    public float elevation;
    public float annualRainfall;
    public float minTemperature;
    public float meanTemperature;
    public int cationExchangeCapacity;
    public int organicCarbonContent;
    public int pH;
    public int clayContent;
    public int siltContent;
    public int sandContent;

    public static Sampler sampler() {
        return new Sampler();
    }

    public static GrowthIndicator byId(String id) {
        switch (id) {
            case "elevation": return p -> p.elevation;
            case "annual_precipitation": return p -> p.annualRainfall;
            case "average_temperature": return p -> p.meanTemperature;
            case "cation_exchange_capacity": return p -> p.cationExchangeCapacity;
            case "organic_carbon_content": return p -> p.organicCarbonContent;
            case "ph": return p -> p.pH;
            case "clay_content": return p -> p.clayContent;
            case "silt_content": return p -> p.siltContent;
            case "sand_content": return p -> p.sandContent;
            default:
                TerrariumEarth.LOGGER.warn("invalid predictor id: {}", id);
                return p -> 0.0;
        }
    }

    public static class Sampler {
        private final FloatRaster.Sampler elevation;
        private final ShortRaster.Sampler annualRainfall;
        private final FloatRaster.Sampler meanTemperature;
        private final FloatRaster.Sampler minTemperature;
        private final UByteRaster.Sampler cationExchangeCapacity;
        private final ShortRaster.Sampler organicCarbonContent;
        private final UByteRaster.Sampler pH;
        private final UByteRaster.Sampler clayContent;
        private final UByteRaster.Sampler siltContent;
        private final UByteRaster.Sampler sandContent;

        Sampler() {
            this.elevation = FloatRaster.sampler(EarthDataKeys.ELEVATION_METERS).defaultValue(0);
            this.annualRainfall = ShortRaster.sampler(EarthDataKeys.ANNUAL_RAINFALL).defaultValue(300);
            this.meanTemperature = FloatRaster.sampler(EarthDataKeys.MEAN_TEMPERATURE).defaultValue(14.0F);
            this.minTemperature = FloatRaster.sampler(EarthDataKeys.MIN_TEMPERATURE).defaultValue(10.0F);
            this.cationExchangeCapacity = UByteRaster.sampler(EarthDataKeys.CATION_EXCHANGE_CAPACITY).defaultValue(10);
            this.organicCarbonContent = ShortRaster.sampler(EarthDataKeys.ORGANIC_CARBON_CONTENT).defaultValue(10);
            this.pH = UByteRaster.sampler(EarthDataKeys.SOIL_PH).defaultValue(70);
            this.clayContent = UByteRaster.sampler(EarthDataKeys.CLAY_CONTENT).defaultValue(33);
            this.siltContent = UByteRaster.sampler(EarthDataKeys.SILT_CONTENT).defaultValue(33);
            this.sandContent = UByteRaster.sampler(EarthDataKeys.SAND_CONTENT).defaultValue(33);
        }

        public GrowthPredictors sample(ColumnDataCache dataCache, int x, int z) {
            GrowthPredictors predictors = new GrowthPredictors();
            this.sampleTo(dataCache, x, z, predictors);
            return predictors;
        }

        public void sampleTo(ColumnDataCache dataCache, int x, int z, GrowthPredictors predictors) {
            predictors.elevation = this.elevation.sample(dataCache, x, z);
            predictors.annualRainfall = this.annualRainfall.sample(dataCache, x, z);
            predictors.meanTemperature = this.meanTemperature.sample(dataCache, x, z);
            predictors.minTemperature = this.minTemperature.sample(dataCache, x, z);
            predictors.cationExchangeCapacity = this.cationExchangeCapacity.sample(dataCache, x, z);
            predictors.organicCarbonContent = this.organicCarbonContent.sample(dataCache, x, z);
            predictors.pH = this.pH.sample(dataCache, x, z);
            predictors.clayContent = this.clayContent.sample(dataCache, x, z);
            predictors.siltContent = this.siltContent.sample(dataCache, x, z);
            predictors.sandContent = this.sandContent.sample(dataCache, x, z);
        }
    }
}
