package net.gegy1000.earth.server.world.ecology;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.Climate;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.ecology.soil.SoilSuborder;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.util.math.ChunkPos;

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
    public int slope;

    public Cover cover = Cover.NO;
    public SoilSuborder soilSuborder = SoilSuborder.NO;
    public Landform landform = Landform.LAND;

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
            case "slope": return p -> p.slope;
            default:
                TerrariumEarth.LOGGER.warn("invalid predictor id: {}", id);
                return p -> 0.0;
        }
    }

    public boolean isSea() {
        return this.landform == Landform.SEA;
    }

    public boolean isRiverOrLake() {
        return this.landform == Landform.LAKE_OR_RIVER;
    }

    public boolean isLand() {
        return this.landform.isLand();
    }

    public boolean isFrozen() {
        return Climate.isFrozen(this.minTemperature, this.meanTemperature) || this.cover.is(CoverMarkers.FROZEN);
    }

    public boolean isCold() {
        return Climate.isCold(this.meanTemperature) || this.isFrozen();
    }

    public boolean isForested() {
        return this.cover.is(CoverMarkers.FOREST);
    }

    public boolean isFlooded() {
        return this.cover.is(CoverMarkers.FLOODED);
    }

    public boolean isBarren() {
        return this.cover.is(CoverMarkers.BARREN);
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
        private final UByteRaster.Sampler slope;

        private final EnumRaster.Sampler<Cover> cover;
        private final EnumRaster.Sampler<SoilSuborder> soilSuborder;
        private final EnumRaster.Sampler<Landform> landform;

        Sampler() {
            this.elevation = FloatRaster.sampler(EarthData.ELEVATION_METERS).defaultValue(-Float.MAX_VALUE);
            this.annualRainfall = ShortRaster.sampler(EarthData.ANNUAL_RAINFALL).defaultValue(300);
            this.meanTemperature = FloatRaster.sampler(EarthData.MEAN_TEMPERATURE).defaultValue(14.0F);
            this.minTemperature = FloatRaster.sampler(EarthData.MIN_TEMPERATURE).defaultValue(10.0F);
            this.cationExchangeCapacity = UByteRaster.sampler(EarthData.CATION_EXCHANGE_CAPACITY).defaultValue(10);
            this.organicCarbonContent = ShortRaster.sampler(EarthData.ORGANIC_CARBON_CONTENT).defaultValue(10);
            this.pH = UByteRaster.sampler(EarthData.SOIL_PH).defaultValue(70);
            this.clayContent = UByteRaster.sampler(EarthData.CLAY_CONTENT).defaultValue(33);
            this.siltContent = UByteRaster.sampler(EarthData.SILT_CONTENT).defaultValue(33);
            this.sandContent = UByteRaster.sampler(EarthData.SAND_CONTENT).defaultValue(33);
            this.slope = UByteRaster.sampler(EarthData.SLOPE).defaultValue(0);
            this.cover = EnumRaster.sampler(EarthData.COVER, Cover.NO);
            this.soilSuborder = EnumRaster.sampler(EarthData.SOIL_SUBORDER, SoilSuborder.NO);
            this.landform = EnumRaster.sampler(EarthData.LANDFORM, Landform.LAND);
        }

        public GrowthPredictors sample(ColumnDataCache dataCache, int x, int z) {
            GrowthPredictors predictors = new GrowthPredictors();
            this.sampleTo(dataCache, x, z, predictors);
            return predictors;
        }

        public GrowthPredictors sample(ColumnData data, int x, int z) {
            GrowthPredictors predictors = new GrowthPredictors();
            this.sampleTo(data, x, z, predictors);
            return predictors;
        }

        public void sampleTo(ColumnDataCache dataCache, int x, int z, GrowthPredictors predictors) {
            ColumnData data = dataCache.joinData(new ChunkPos(x >> 4, z >> 4));
            this.sampleTo(data, x & 0xF, z & 0xF, predictors);
        }

        public void sampleTo(ColumnData data, int x, int z, GrowthPredictors predictors) {
            predictors.elevation = this.elevation.sample(data, x, z);
            predictors.annualRainfall = this.annualRainfall.sample(data, x, z);
            predictors.meanTemperature = this.meanTemperature.sample(data, x, z);
            predictors.minTemperature = this.minTemperature.sample(data, x, z);
            predictors.cationExchangeCapacity = this.cationExchangeCapacity.sample(data, x, z);
            predictors.organicCarbonContent = this.organicCarbonContent.sample(data, x, z);
            predictors.pH = this.pH.sample(data, x, z);
            predictors.clayContent = this.clayContent.sample(data, x, z);
            predictors.siltContent = this.siltContent.sample(data, x, z);
            predictors.sandContent = this.sandContent.sample(data, x, z);
            predictors.slope = this.slope.sample(data, x, z);
            predictors.cover = this.cover.sample(data, x, z);
            predictors.soilSuborder = this.soilSuborder.sample(data, x, z);
            predictors.landform = this.landform.sample(data, x, z);
        }
    }
}
