package net.gegy1000.earth.server.command.debugger;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.util.debug.CoverColors;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.util.debug.SoilColors;
import net.gegy1000.earth.server.world.ecology.soil.SoilSuborder;
import net.gegy1000.earth.server.world.ecology.vegetation.Vegetation;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.NumberRaster;
import net.gegy1000.terrarium.server.world.data.raster.Raster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;

public final class GeoDebugger {
    private final TerrariumWorld terrarium;
    private final EarthWorld earth;

    private GeoDebugger(TerrariumWorld terrarium, EarthWorld earth) {
        this.terrarium = terrarium;
        this.earth = earth;
    }

    public static GeoDebugger from(World world) {
        TerrariumWorld terrarium = TerrariumWorld.get(world);
        Preconditions.checkNotNull(terrarium, "terrarium world was null");

        EarthWorld earth = EarthWorld.get(world);
        Preconditions.checkNotNull(earth, "earth world was null");

        return new GeoDebugger(terrarium, earth);
    }

    public RasterSampler vegetation(String name, Vegetation vegetation) {
        return this.heatmap(name, (dataCache, view) -> {
            GrowthIndicator indicator = vegetation.getGrowthIndicator();

            GrowthPredictors predictors = new GrowthPredictors();
            GrowthPredictors.Sampler predictorSampler = GrowthPredictors.sampler();

            UByteRaster raster = UByteRaster.create(view);

            for (int y = 0; y < view.getHeight(); y++) {
                for (int x = 0; x < view.getWidth(); x++) {
                    int blockX = view.getMinX() + x;
                    int blockZ = view.getMinY() + y;
                    predictorSampler.sampleTo(dataCache, blockX, blockZ, predictors);

                    if (predictors.elevation >= 0.0) {
                        double suitabilityIndex = indicator.evaluate(predictors);
                        raster.set(x, y, MathHelper.floor(suitabilityIndex * 255.0));
                    }
                }
            }

            return raster;
        });
    }

    public <R extends NumberRaster<?>> RasterSampler scaledHeatmap(String name, Raster.Sampler<R> sampler) {
        return this.heatmap(name, (dataCache, view) -> {
            R source = sampler.sample(dataCache, view);
            double min = source.stream().min().orElse(0.0);
            double max = source.stream().max().orElse(0.0);

            UByteRaster result = UByteRaster.create(view);
            for (int y = 0; y < view.getHeight(); y++) {
                for (int x = 0; x < view.getWidth(); x++) {
                    double value = source.getDouble(x, y);
                    result.set(x, y, MathHelper.floor((value - min) / (max - min) * 255.0));
                }
            }

            return result;
        });
    }

    public RasterSampler heatmap(String name, Raster.Sampler<UByteRaster> sampler) {
        return new RasterSampler(name, (dataCache, view) -> {
            BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);
            UByteRaster raster = sampler.apply(dataCache, view);
            raster.iterate((value, x, y) -> {
                image.setRGB(x, y, 255 << 16 | value << 8);
            });
            return image;
        });
    }

    public RasterSampler cover(String name, EnumRaster.Sampler<Cover> sampler) {
        return new RasterSampler(name, (dataCache, view) -> {
            BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);

            EnumRaster<Cover> raster = sampler.sample(dataCache, view);
            raster.iterate((cover, x, y) -> {
                image.setRGB(x, y, CoverColors.get(cover));
            });

            return image;
        });
    }

    public RasterSampler soilSuborder(String name, EnumRaster.Sampler<SoilSuborder> sampler) {
        return new RasterSampler(name, (dataCache, view) -> {
            BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_INT_RGB);

            EnumRaster<SoilSuborder> raster = sampler.sample(dataCache, view);
            raster.iterate((cover, x, y) -> {
                image.setRGB(x, y, SoilColors.get(cover));
            });

            return image;
        });
    }

    public DebugGeoProfile[] takeTestProfiles() {
        DebugProfileTestSet.Location[] testSet = DebugProfileTestSet.get();

        DebugGeoProfile[] profiles = new DebugGeoProfile[testSet.length];
        for (int i = 0; i < testSet.length; i++) {
            DebugProfileTestSet.Location location = testSet[i];
            Coordinate coordinate = new Coordinate(this.earth.getCrs(), location.longitude, location.latitude);
            profiles[i] = this.takeProfile(location.name, coordinate.getBlockX(), coordinate.getBlockZ());
        }

        return profiles;
    }

    public DebugGeoProfile takeProfile(String name, double x, double z) {
        ColumnDataCache data = this.terrarium.getDataCache();

        Coordinate coordinate = Coordinate.atBlock(x, z).to(this.earth.getCrs());
        double latitude = coordinate.getZ();
        double longitude = coordinate.getX();

        int bx = MathHelper.floor(x);
        int bz = MathHelper.floor(z);

        float elevation = FloatRaster.sampler(EarthData.ELEVATION_METERS).sample(data, bx, bz);
        Cover cover = EnumRaster.sampler(EarthData.COVER, Cover.NO).sample(data, bx, bz);
        float meanTemperature = FloatRaster.sampler(EarthData.MEAN_TEMPERATURE).sample(data, bx, bz);
        float minTemperature = FloatRaster.sampler(EarthData.MIN_TEMPERATURE).sample(data, bx, bz);
        short annualRainfall = ShortRaster.sampler(EarthData.ANNUAL_RAINFALL).sample(data, bx, bz);

        SoilSuborder soilSuborder = EnumRaster.sampler(EarthData.SOIL_SUBORDER, SoilSuborder.NO).sample(data, bx, bz);
        int siltContent = UByteRaster.sampler(EarthData.SILT_CONTENT).sample(data, bx, bz);
        int sandContent = UByteRaster.sampler(EarthData.SAND_CONTENT).sample(data, bx, bz);
        int clayContent = UByteRaster.sampler(EarthData.CLAY_CONTENT).sample(data, bx, bz);
        short organicCarbonContent = ShortRaster.sampler(EarthData.ORGANIC_CARBON_CONTENT).sample(data, bx, bz);
        int cationExchangeCapacity = UByteRaster.sampler(EarthData.CATION_EXCHANGE_CAPACITY).sample(data, bx, bz);
        int soilPh = UByteRaster.sampler(EarthData.SOIL_PH).sample(data, bx, bz);

        return new DebugGeoProfile(
                name, latitude, longitude,
                elevation, cover,
                meanTemperature, minTemperature, annualRainfall,
                soilSuborder,
                siltContent, sandContent, clayContent,
                organicCarbonContent, cationExchangeCapacity, soilPh / 10.0F
        );
    }

    public static class RasterSampler {
        public final String name;
        private final Raster.Sampler<BufferedImage> function;

        public RasterSampler(String name, Raster.Sampler<BufferedImage> sampler) {
            this.name = name;
            this.function = sampler;
        }

        public BufferedImage sample(ColumnDataCache data, DataView view) {
            return this.function.apply(data, view);
        }
    }
}
