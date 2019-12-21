package net.gegy1000.earth.server.world.ecology;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.EarthDataKeys;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;

public final class GrowthPredictors {
    public short elevation;
    public float annualRainfall;
    public float averageTemperature;
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
            case "average_temperature": return p -> p.averageTemperature;
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
        private final ShortRaster.Sampler elevation;
        private final ShortRaster.Sampler annualRainfall;
        private final FloatRaster.Sampler averageTemperature;
        private final UByteRaster.Sampler cationExchangeCapacity;
        private final ShortRaster.Sampler organicCarbonContent;
        private final UByteRaster.Sampler pH;
        private final UByteRaster.Sampler clayContent;
        private final UByteRaster.Sampler siltContent;
        private final UByteRaster.Sampler sandContent;

        Sampler() {
            this.elevation = ShortRaster.sampler(EarthDataKeys.ELEVATION_METERS);
            this.annualRainfall = ShortRaster.sampler(EarthDataKeys.ANNUAL_RAINFALL);
            this.averageTemperature = FloatRaster.sampler(EarthDataKeys.MEAN_TEMPERATURE);
            this.cationExchangeCapacity = UByteRaster.sampler(EarthDataKeys.CATION_EXCHANGE_CAPACITY);
            this.organicCarbonContent = ShortRaster.sampler(EarthDataKeys.ORGANIC_CARBON_CONTENT);
            this.pH = UByteRaster.sampler(EarthDataKeys.SOIL_PH);
            this.clayContent = UByteRaster.sampler(EarthDataKeys.CLAY_CONTENT);
            this.siltContent = UByteRaster.sampler(EarthDataKeys.SILT_CONTENT);
            this.sandContent = UByteRaster.sampler(EarthDataKeys.SAND_CONTENT);
        }

        public void sampleTo(ColumnDataCache dataCache, int x, int z, GrowthPredictors predictors) {
            predictors.elevation = this.elevation.sample(dataCache, x, z);
            predictors.annualRainfall = this.annualRainfall.sample(dataCache, x, z);
            predictors.averageTemperature = this.averageTemperature.sample(dataCache, x, z);
            predictors.cationExchangeCapacity = this.cationExchangeCapacity.sample(dataCache, x, z);
            predictors.organicCarbonContent = this.organicCarbonContent.sample(dataCache, x, z);
            predictors.pH = this.pH.sample(dataCache, x, z);
            predictors.clayContent = this.clayContent.sample(dataCache, x, z);
            predictors.siltContent = this.siltContent.sample(dataCache, x, z);
            predictors.sandContent = this.sandContent.sample(dataCache, x, z);
        }
    }
}
