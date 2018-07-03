package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.EarthComponentTypes;
import net.gegy1000.earth.server.world.pipeline.adapter.OceanDepthCorrectionAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.OsmCoastlineAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WaterFlattenAdapter;
import net.gegy1000.earth.server.world.pipeline.composer.BoulderDecorationComposer;
import net.gegy1000.earth.server.world.pipeline.populator.OsmRegionPopulator;
import net.gegy1000.earth.server.world.pipeline.sampler.OsmSampler;
import net.gegy1000.earth.server.world.pipeline.source.GlobcoverSource;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.gegy1000.earth.server.world.pipeline.source.osm.OverpassSource;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorInitializer;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.LatLngCoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.ScaledCoordinateState;
import net.gegy1000.terrarium.server.world.cover.ConstructedCover;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.generator.BasicTerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertyContainer;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.widget.InversePropertyConverter;
import net.gegy1000.terrarium.server.world.generator.customization.widget.SliderWidget;
import net.gegy1000.terrarium.server.world.generator.customization.widget.ToggleWidget;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.adapter.BeachAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.HeightNoiseAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.HeightTransformAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.CoverBiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.CoverDecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.BedrockSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CoverSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.HeightmapSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.OceanFillSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.populator.ScaledByteRegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.populator.ScaledCoverRegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.populator.ScaledShortRegionPopulator;
import net.gegy1000.terrarium.server.world.pipeline.sampler.CoverTileSampler;
import net.gegy1000.terrarium.server.world.pipeline.sampler.ShortTileSampler;
import net.gegy1000.terrarium.server.world.pipeline.sampler.SlopeTileSampler;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EarthWorldType extends TerrariumWorldType {
    private static final double EARTH_CIRCUMFERENCE = 40075000.0;
    private static final double SRTM_WIDTH = 1200.0 * 360.0;
    private static final double SRTM_SCALE = EARTH_CIRCUMFERENCE / SRTM_WIDTH;
    private static final double GLOB_RATIO = 10.0 / 3.0;

    private static final ResourceLocation IDENTIFIER = new ResourceLocation(TerrariumEarth.MODID, "earth_generator");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.MODID, "earth_default");

    public static final PropertyKey<Number> SPAWN_LATITUDE = PropertyKey.createNumber("spawn_latitude");
    public static final PropertyKey<Number> SPAWN_LONGITUDE = PropertyKey.createNumber("spawn_longitude");
    public static final PropertyKey<Boolean> ENABLE_DECORATION = PropertyKey.createBoolean("enable_decoration");
    public static final PropertyKey<Number> WORLD_SCALE = PropertyKey.createNumber("world_scale");
    public static final PropertyKey<Number> HEIGHT_SCALE = PropertyKey.createNumber("height_scale");
    public static final PropertyKey<Number> NOISE_SCALE = PropertyKey.createNumber("noise_scale");
    public static final PropertyKey<Number> HEIGHT_ORIGIN = PropertyKey.createNumber("height_origin");
    public static final PropertyKey<Number> OCEAN_DEPTH = PropertyKey.createNumber("ocean_depth");
    public static final PropertyKey<Number> BEACH_SIZE = PropertyKey.createNumber("beach_size");
    public static final PropertyKey<Boolean> ENABLE_BUILDINGS = PropertyKey.createBoolean("enable_buildings");
    public static final PropertyKey<Boolean> ENABLE_STREETS = PropertyKey.createBoolean("enable_streets");
    public static final PropertyKey<Boolean> ENABLE_VANILLA_FEATURES = PropertyKey.createBoolean("enable_vanilla_features");
    public static final PropertyKey<Boolean> ENABLE_CAVE_GENERATION = PropertyKey.createBoolean("enable_cave_generation");
    public static final PropertyKey<Boolean> ENABLE_RESOURCE_GENERATION = PropertyKey.createBoolean("enable_resource_generation");

    public EarthWorldType() {
        super("earth", IDENTIFIER, PRESET);
    }

    @Override
    public TerrariumGeneratorInitializer createInitializer(World world, GenerationSettings settings) {
        return new Initializer(world, settings.getProperties());
    }

    @Override
    protected TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.builder()
                .withCategory("world",
                        new SliderWidget(WORLD_SCALE, 1.0, 200.0, 5.0, 1.0, new InversePropertyConverter()),
                        new SliderWidget(HEIGHT_SCALE, 0.0, 10.0, 0.5, 0.1),
                        new SliderWidget(NOISE_SCALE, 0.0, 3.0, 0.5, 0.1),
                        new SliderWidget(OCEAN_DEPTH, 0, 32, 1, 1),
                        new SliderWidget(HEIGHT_ORIGIN, 0, 255, 1, 1),
                        new SliderWidget(BEACH_SIZE, 0, 8, 1, 1)
                )
                .withCategory("features",
                        new ToggleWidget(ENABLE_DECORATION),
                        new ToggleWidget(ENABLE_BUILDINGS),
                        new ToggleWidget(ENABLE_STREETS),
                        new ToggleWidget(ENABLE_VANILLA_FEATURES),
                        new ToggleWidget(ENABLE_CAVE_GENERATION),
                        new ToggleWidget(ENABLE_RESOURCE_GENERATION)
                )
                .build();
    }

    @Override
    protected GuiScreen createCustomizationGui(GuiCreateWorld parent, TerrariumPreset preset) {
        return new EarthCustomizationGui(parent, this, preset);
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
        TerrariumWorldData worldData = this.getWorldData(world);
        return worldData.getSettings().getProperties().getInteger(HEIGHT_ORIGIN) < 40;
    }

    private static class Initializer implements TerrariumGeneratorInitializer {
        private final World world;
        private final PropertyContainer properties;

        private final double worldScale;

        private final CoordinateState earthCoordinates;
        private final CoordinateState srtmRaster;
        private final CoordinateState globcoverRaster;

        private Initializer(World world, PropertyContainer properties) {
            this.world = world;
            this.properties = properties;

            this.worldScale = properties.getDouble(WORLD_SCALE);
            this.earthCoordinates = new LatLngCoordinateState(this.worldScale * SRTM_SCALE * 1200.0);
            this.srtmRaster = new ScaledCoordinateState(this.worldScale * SRTM_SCALE);
            this.globcoverRaster = new ScaledCoordinateState(this.worldScale * SRTM_SCALE * GLOB_RATIO);
        }

        @Override
        public TerrariumGenerator buildGenerator() {
            int heightOrigin = this.properties.getInteger(HEIGHT_ORIGIN);
            List<ConstructedCover<?>> coverTypes = this.constructCoverTypes();
            BasicTerrariumGenerator.Builder builder = BasicTerrariumGenerator.builder()
                    .withSurfaceComposer(new HeightmapSurfaceComposer(RegionComponentType.HEIGHT, Blocks.STONE.getDefaultState()))
                    .withSurfaceComposer(new OceanFillSurfaceComposer(RegionComponentType.HEIGHT, Blocks.WATER.getDefaultState(), heightOrigin + 1))
                    .withSurfaceComposer(new CoverSurfaceComposer(this.world, RegionComponentType.COVER, coverTypes, this.properties.getBoolean(ENABLE_DECORATION), Blocks.STONE.getDefaultState()))
                    .withSurfaceComposer(new BedrockSurfaceComposer(this.world, Blocks.BEDROCK.getDefaultState(), Math.min(heightOrigin - 1, 5)))
                    .withBiomeComposer(new CoverBiomeComposer(RegionComponentType.COVER, coverTypes))
                    .withSpawnPosition(new Coordinate(this.earthCoordinates, this.properties.getDouble(SPAWN_LATITUDE), this.properties.getDouble(SPAWN_LONGITUDE)))
                    .withCapability(new EarthCapability.Impl(this.earthCoordinates));
            if (this.properties.getBoolean(ENABLE_DECORATION)) {
                builder.withDecorationComposer(new CoverDecorationComposer(this.world, RegionComponentType.COVER, coverTypes));
                builder.withDecorationComposer(new BoulderDecorationComposer(this.world, RegionComponentType.SLOPE));
            }
            return builder.build();
        }

        private List<ConstructedCover<?>> constructCoverTypes() {
            List<ConstructedCover<?>> coverTypes = new ArrayList<>();
            CoverGenerationContext.Default context = new CoverGenerationContext.Default(this.world, RegionComponentType.HEIGHT, RegionComponentType.COVER);
            EarthCoverContext earthContext = new EarthCoverContext(this.world, RegionComponentType.HEIGHT, RegionComponentType.COVER, RegionComponentType.SLOPE, this.earthCoordinates, true);
            coverTypes.add(new ConstructedCover<>(TerrariumCoverTypes.DEBUG, context));
            coverTypes.add(new ConstructedCover<>(TerrariumCoverTypes.PLACEHOLDER, context));
            coverTypes.addAll(EarthCoverTypes.COVER_TYPES.stream().map(type -> new ConstructedCover<>(type, earthContext)).collect(Collectors.toList()));
            return coverTypes;
        }

        @Override
        public TerrariumDataProvider buildDataProvider() {
            int heightOrigin = this.properties.getInteger(HEIGHT_ORIGIN);
            SrtmHeightSource heightSource = new SrtmHeightSource(this.srtmRaster, "srtm_heights");
            return TerrariumDataProvider.builder()
                    .withComponent(RegionComponentType.HEIGHT, this.createHeightPopulator(heightSource))
                    .withComponent(RegionComponentType.SLOPE, this.createSlopePopulator(heightSource))
                    .withComponent(RegionComponentType.COVER, this.createCoverPopulator())
                    .withComponent(EarthComponentTypes.OSM, this.createOsmPopulator())
                    .withAdapter(new OsmCoastlineAdapter(RegionComponentType.HEIGHT, RegionComponentType.COVER, EarthComponentTypes.OSM, this.earthCoordinates))
                    .withAdapter(new HeightNoiseAdapter(this.world, RegionComponentType.HEIGHT, 2, 0.08, this.properties.getDouble(NOISE_SCALE)))
                    .withAdapter(new HeightTransformAdapter(RegionComponentType.HEIGHT, this.properties.getDouble(HEIGHT_SCALE) * this.worldScale, heightOrigin))
                    .withAdapter(new OceanDepthCorrectionAdapter(RegionComponentType.HEIGHT, this.properties.getInteger(OCEAN_DEPTH)))
                    .withAdapter(new BeachAdapter(this.world, RegionComponentType.COVER, this.properties.getInteger(BEACH_SIZE), EarthCoverTypes.WATER, EarthCoverTypes.BEACH))
                    .withAdapter(new WaterFlattenAdapter(RegionComponentType.HEIGHT, RegionComponentType.COVER, 15, EarthCoverTypes.WATER))
                    .build();
        }

        private ScaledShortRegionPopulator createHeightPopulator(SrtmHeightSource heightSource) {
            Interpolation.Method interpolationMethod = this.selectInterpolationMethod(this.properties);
            ShortTileSampler heightSampler = new ShortTileSampler(heightSource);
            return new ScaledShortRegionPopulator(heightSampler, this.srtmRaster, interpolationMethod);
        }

        private ScaledByteRegionPopulator createSlopePopulator(SrtmHeightSource heightSource) {
            SlopeTileSampler slopeSampler = new SlopeTileSampler(heightSource);
            return new ScaledByteRegionPopulator(slopeSampler, this.srtmRaster, Interpolation.Method.LINEAR);
        }

        private ScaledCoverRegionPopulator createCoverPopulator() {
            GlobcoverSource globcoverSource = new GlobcoverSource(this.globcoverRaster, "globcover");
            CoverTileSampler coverSampler = new CoverTileSampler(globcoverSource);
            return new ScaledCoverRegionPopulator(coverSampler, this.globcoverRaster);
        }

        private OsmRegionPopulator createOsmPopulator() {
            OverpassSource outlineSource = new OverpassSource(
                    this.earthCoordinates,
                    0.3,
                    "osm/outline",
                    new ResourceLocation(TerrariumEarth.MODID, "query/outline_overpass_query.oql"),
                    1
            );
            OverpassSource generalSource = new OverpassSource(
                    this.earthCoordinates,
                    0.1,
                    "osm/general",
                    new ResourceLocation(TerrariumEarth.MODID, "query/general_overpass_query.oql"),
                    2
            );
            OverpassSource detailSource = new OverpassSource(
                    this.earthCoordinates,
                    0.05,
                    "osm/detailed",
                    new ResourceLocation(TerrariumEarth.MODID, "query/detail_overpass_query.oql"),
                    2
            );
            return new OsmRegionPopulator(
                    new OsmSampler(outlineSource, this.earthCoordinates),
                    new OsmSampler(generalSource, this.earthCoordinates),
                    new OsmSampler(detailSource, this.earthCoordinates)
            );
        }

        private Interpolation.Method selectInterpolationMethod(PropertyContainer properties) {
            double scale = 1.0 / properties.getDouble(WORLD_SCALE);

            Interpolation.Method interpolationMethod = Interpolation.Method.CUBIC;
            if (scale >= 45.0) {
                interpolationMethod = Interpolation.Method.LINEAR;
            } else if (scale >= 20.0) {
                interpolationMethod = Interpolation.Method.COSINE;
            }

            return interpolationMethod;
        }
    }
}
