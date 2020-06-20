package net.gegy1000.earth.server.util.debug;

import net.gegy1000.earth.server.integration.bop.BoPTrees;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.zoom.Zoomable;
import net.gegy1000.earth.server.world.biome.BiomeClassifier;
import net.gegy1000.earth.server.world.biome.StandardBiomeClassifier;
import net.gegy1000.earth.server.world.cover.Cover;
import net.gegy1000.earth.server.world.cover.CoverMarkers;
import net.gegy1000.earth.server.world.data.source.StdSource;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.earth.server.world.ecology.GrowthIndicator;
import net.gegy1000.earth.server.world.ecology.GrowthPredictors;
import net.gegy1000.earth.server.world.ecology.soil.SoilSuborder;
import net.gegy1000.earth.server.world.ecology.vegetation.Trees;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.Raster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.raster.UByteRaster;
import net.minecraft.world.biome.Biome;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static net.gegy1000.earth.server.world.EarthDataInitializer.*;

final class RasterDebug {
    private static final int TILE_SIZE = StdSource.TILE_SIZE;
    private static final Path OUTPUT = Paths.get("mods/terrarium/debug");

    private static final Vegetation[] TREES = new Vegetation[] {
            new Vegetation(Trees.Indicators.ACACIA, new Color(255, 184, 63)),
            new Vegetation(Trees.Indicators.BIRCH, new Color(143, 255, 180)),
            new Vegetation(Trees.Indicators.JUNGLE_LIKE, new Color(42, 175, 0)),
            new Vegetation(Trees.Indicators.OAK, new Color(78, 145, 77)),
            new Vegetation(Trees.Indicators.PINE, new Color(40, 118, 79)),
            new Vegetation(Trees.Indicators.SPRUCE, new Color(36, 99, 64)),
            new Vegetation(BoPTrees.Indicators.EBONY, new Color(87, 62, 58)),
            new Vegetation(BoPTrees.Indicators.FIR, new Color(44, 116, 109)),
            new Vegetation(BoPTrees.Indicators.MAHOGANY, new Color(116, 76, 80)),
            new Vegetation(BoPTrees.Indicators.PALM, new Color(141, 199, 50)),
            new Vegetation(BoPTrees.Indicators.EUCALYPTUS, new Color(255, 111, 0)),
            new Vegetation(BoPTrees.Indicators.MANGROVE, new Color(78, 199, 183)),
            new Vegetation(BoPTrees.Indicators.WILLOW, new Color(32, 84, 84)),
    };

    public static void main(String[] args) throws IOException {
        Files.createDirectories(OUTPUT);

        DebugBootstrap.run();

        System.out.println("loading rasters");
        Rasters rasters = new Rasters();

        System.out.println("rendering soil suborder masks");
        renderSoilMasks(rasters);

        System.out.println("rendering merged soils");
        renderEnumRaster("global_soil", rasters.soil, SoilColors::get);

        System.out.println("rendering merged cover");
        renderEnumRaster("global_cover", rasters.cover, CoverColors::get);

        System.out.println("rendering dominant tree layer");
        renderDominantTrees(rasters);

        System.out.println("rendering biome layer");
        renderBiomes(rasters);
    }

    private static <E extends Enum<E>> void renderEnumRaster(String name, EnumRaster<E> raster, ToIntFunction<E> color) throws IOException {
        Path path = OUTPUT.resolve(name + ".png");
        if (Files.exists(path)) {
            return;
        }

        BufferedImage maskImage = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_INT_RGB);

        raster.iterate((value, x, y) -> {
            maskImage.setRGB(x, y, color.applyAsInt(value));
        });

        ImageIO.write(maskImage, "png", path.toFile());
    }

    private static void renderSoilMasks(Rasters rasters) throws IOException {
        int width = rasters.soil.getWidth();
        int height = rasters.soil.getHeight();

        Path root = OUTPUT.resolve("suborder_masks");
        Files.createDirectories(root);

        for (SoilSuborder maskClass : SoilSuborder.values()) {
            Path path = root.resolve(maskClass.name().toLowerCase(Locale.ROOT) + ".png");
            if (Files.exists(path)) {
                continue;
            }

            BufferedImage maskImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            rasters.soil.iterate((value, x, y) -> {
                if (value == maskClass) {
                    maskImage.setRGB(x, y, 0xFF0000);
                } else {
                    Cover cover = rasters.cover.get(x, y);
                    maskImage.setRGB(x, y, cover == Cover.WATER ? 0x0000FF : 0x000000);
                }
            });

            ImageIO.write(maskImage, "png", path.toFile());
        }
    }

    private static void renderDominantTrees(Rasters rasters) throws IOException {
        int width = rasters.elevation.getWidth();
        int height = rasters.elevation.getHeight();

        BufferedImage dominantTreeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        GrowthPredictors predictors = new GrowthPredictors();
        rasters.elevation.iterate((elevation, x, y) -> {
            if (elevation >= 0) {
                rasters.samplePredictorsTo(predictors, x, y);

                double dominantIndicator = 0.0;
                Vegetation domininantTree = null;

                for (Vegetation tree : TREES) {
                    double indicator = tree.indicator.evaluate(predictors);
                    if (indicator > dominantIndicator) {
                        dominantIndicator = indicator;
                        domininantTree = tree;
                    }
                }

                if (domininantTree != null) {
                    dominantTreeImage.setRGB(x, y, domininantTree.color.getRGB());
                }
            } else {
                dominantTreeImage.setRGB(x, y, 0x0000FF);
            }
        });

        ImageIO.write(dominantTreeImage, "png", OUTPUT.resolve("global_dominant_tree.png").toFile());
    }

    private static void renderBiomes(Rasters rasters) throws IOException {
        int width = rasters.elevation.getWidth();
        int height = rasters.elevation.getHeight();

        BufferedImage biomeImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        BiomeClassifier classifier = new StandardBiomeClassifier();
        GrowthPredictors predictors = new GrowthPredictors();

        rasters.elevation.iterate((elevation, x, y) -> {
            rasters.samplePredictorsTo(predictors, x, y);

            Biome biome = classifier.classify(predictors);
            biomeImage.setRGB(x, y, BiomeColors.get(biome));
        });

        ImageIO.write(biomeImage, "png", OUTPUT.resolve("global_biomes.png").toFile());
    }

    private static FloatRaster sampleMinTemperature(WorldClimateRaster source) {
        FloatRaster dst = FloatRaster.create(DataView.rect(TILE_SIZE * 2, TILE_SIZE));
        dst.transform((value, x, y) -> {
            int climateX = x * source.getWidth() / dst.getWidth();
            int climateY = y * source.getHeight() / dst.getHeight();
            return source.getMinTemperature(climateX, climateY);
        });
        return dst;
    }

    private static FloatRaster sampleMeanTemperature(WorldClimateRaster source) {
        FloatRaster dst = FloatRaster.create(DataView.rect(TILE_SIZE * 2, TILE_SIZE));
        dst.transform((value, x, y) -> {
            int climateX = x * source.getWidth() / dst.getWidth();
            int climateY = y * source.getHeight() / dst.getHeight();
            return source.getMeanTemperature(climateX, climateY);
        });
        return dst;
    }

    private static ShortRaster sampleAnnualRainfall(WorldClimateRaster source) {
        ShortRaster dst = ShortRaster.create(DataView.rect(TILE_SIZE * 2, TILE_SIZE));
        dst.transform((value, x, y) -> {
            int climateX = x * source.getWidth() / dst.getWidth();
            int climateY = y * source.getHeight() / dst.getHeight();
            return source.getAnnualRainfall(climateX, climateY);
        });
        return dst;
    }

    static <R extends Raster<?>> R sampleGlobal(
            Zoomable<StdSource<R>> source,
            Function<DataView, R> createRaster
    ) throws IOException {
        R global = createRaster.apply(DataView.rect(TILE_SIZE * 2, TILE_SIZE));

        for (int x = 0; x < 2; x++) {
            DataView srcView = DataView.square(x * TILE_SIZE, 0, TILE_SIZE);
            source.forZoom(0).load(new Vec2i(x, 0)).ifPresent(tile -> {
                Raster.rasterCopy(tile, srcView, global, global.asView());
            });
        }

        return global;
    }

    static class Rasters {
        final FloatRaster elevation;
        final EnumRaster<Cover> cover;
        final EnumRaster<SoilSuborder> soil;
        final ShortRaster cec;
        final ShortRaster occ;
        final UByteRaster ph;
        final UByteRaster clay;
        final UByteRaster silt;
        final UByteRaster sand;
        final ShortRaster annualRainfall;
        final FloatRaster minTemperature;
        final FloatRaster meanTemperature;

        Rasters() throws IOException {
            this.elevation = sampleGlobal(ELEVATION_SOURCE, FloatRaster::create);
            this.cover = sampleGlobal(LAND_COVER_SOURCE, view -> EnumRaster.create(Cover.NO, view));
            this.soil = sampleGlobal(SOIL_CLASS_SOURCE, view -> EnumRaster.create(SoilSuborder.NO, view));

            this.cec = sampleGlobal(CATION_EXCHANGE_CAPACITY_SOURCE, ShortRaster::create);
            this.occ = sampleGlobal(ORGANIC_CARBON_CONTENT_SOURCE, ShortRaster::create);
            this.ph = sampleGlobal(PH_SOURCE, UByteRaster::create);
            this.clay = sampleGlobal(CLAY_CONTENT_SOURCE, UByteRaster::create);
            this.silt = sampleGlobal(SILT_CONTENT_SOURCE, UByteRaster::create);
            this.sand = sampleGlobal(SAND_CONTENT_SOURCE, UByteRaster::create);

            WorldClimateRaster climateRaster = SharedEarthData.instance().get(SharedEarthData.CLIMATIC_VARIABLES);
            if (climateRaster == null) throw new IllegalStateException();

            this.annualRainfall = sampleAnnualRainfall(climateRaster);
            this.minTemperature = sampleMinTemperature(climateRaster);
            this.meanTemperature = sampleMeanTemperature(climateRaster);
        }

        void samplePredictorsTo(GrowthPredictors predictors, int x, int y) {
            predictors.elevation = this.elevation.get(x, y);
            predictors.annualRainfall = this.annualRainfall.get(x, y);
            predictors.minTemperature = this.minTemperature.get(x, y);
            predictors.meanTemperature = this.meanTemperature.get(x, y);
            predictors.cationExchangeCapacity = this.cec.get(x, y);
            predictors.organicCarbonContent = this.occ.get(x, y);
            predictors.pH = this.ph.get(x, y);
            predictors.clayContent = this.clay.get(x, y);
            predictors.siltContent = this.silt.get(x, y);
            predictors.sandContent = this.sand.get(x, y);
            predictors.slope = 0;
            predictors.cover = this.cover.get(x, y);
            predictors.soilSuborder = this.soil.get(x, y);

            predictors.landform = predictors.elevation <= 0.0F ? Landform.SEA : Landform.LAND;
            if (predictors.landform.isLand() && predictors.cover.is(CoverMarkers.WATER)) {
                predictors.landform = Landform.LAKE_OR_RIVER;
            }
        }
    }

    static class Vegetation {
        private final GrowthIndicator indicator;
        private final Color color;

        Vegetation(GrowthIndicator indicator, Color color) {
            this.indicator = indicator;
            this.color = color;
        }
    }
}
