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

    // TODO: These builders are absolutely horrible
    @Override
    public TerrariumGenerator buildGenerator(World world, GenerationSettings settings) {
        PropertyContainer properties = settings.getProperties();
        double worldScale = properties.getDouble(WORLD_SCALE);
        int heightOrigin = properties.getInteger(HEIGHT_ORIGIN);
        CoordinateState earthCoordinates = new LatLngCoordinateState(worldScale * SRTM_SCALE * 1200.0);
        List<ConstructedCover<?>> coverTypes = new ArrayList<>();
        CoverGenerationContext.Default context = new CoverGenerationContext.Default(world, RegionComponentType.HEIGHT, RegionComponentType.COVER);
        EarthCoverContext earthContext = new EarthCoverContext(world, RegionComponentType.HEIGHT, RegionComponentType.COVER, RegionComponentType.SLOPE, earthCoordinates, true);
        coverTypes.add(new ConstructedCover<>(TerrariumCoverTypes.DEBUG, context));
        coverTypes.add(new ConstructedCover<>(TerrariumCoverTypes.PLACEHOLDER, context));
        coverTypes.addAll(EarthCoverTypes.COVER_TYPES.stream().map(type -> new ConstructedCover<>(type, earthContext)).collect(Collectors.toList()));
        return BasicTerrariumGenerator.builder()
                .withSurfaceComposer(new HeightmapSurfaceComposer(RegionComponentType.HEIGHT, Blocks.STONE.getDefaultState()))
                .withSurfaceComposer(new OceanFillSurfaceComposer(RegionComponentType.HEIGHT, Blocks.WATER.getDefaultState(), heightOrigin + 1))
                .withSurfaceComposer(new CoverSurfaceComposer(world, RegionComponentType.COVER, coverTypes, properties.getBoolean(ENABLE_DECORATION), Blocks.STONE.getDefaultState()))
                .withSurfaceComposer(new BedrockSurfaceComposer(world, Blocks.BEDROCK.getDefaultState(), Math.min(heightOrigin - 1, 5)))
                .withDecorationComposer(new CoverDecorationComposer(world, RegionComponentType.COVER, coverTypes)) // TODO: Decoration composers only if decoration enabled!
                .withDecorationComposer(new BoulderDecorationComposer(world, RegionComponentType.SLOPE))
                .withBiomeComposer(new CoverBiomeComposer(RegionComponentType.COVER, coverTypes))
                .withSpawnPosition(new Coordinate(earthCoordinates, properties.getDouble(SPAWN_LATITUDE), properties.getDouble(SPAWN_LONGITUDE)))
                .withCapability(new EarthCapability.Impl(earthCoordinates))
                .build();
    }

    @Override
    public TerrariumDataProvider buildDataProvider(World world, GenerationSettings settings) {
        PropertyContainer properties = settings.getProperties();
        double worldScale = properties.getDouble(WORLD_SCALE);
        int heightOrigin = properties.getInteger(HEIGHT_ORIGIN);
        // TODO: Avoid the duplication of this in both buildGenerator and buildDataProvider
        CoordinateState earthCoordinates = new LatLngCoordinateState(worldScale * SRTM_SCALE * 1200.0);
        CoordinateState srtmRaster = new ScaledCoordinateState(worldScale * SRTM_SCALE);
        CoordinateState globcoverRaster = new ScaledCoordinateState(worldScale * SRTM_SCALE * GLOB_RATIO);
        Interpolation.Method interpolationMethod = this.selectInterpolationMethod(settings);
        SrtmHeightSource heightSource = new SrtmHeightSource(srtmRaster, "srtm_heights");
        return TerrariumDataProvider.builder()
                .withComponent(RegionComponentType.HEIGHT, new ScaledShortRegionPopulator(new ShortTileSampler(heightSource), srtmRaster, interpolationMethod))
                .withComponent(RegionComponentType.SLOPE, new ScaledByteRegionPopulator(new SlopeTileSampler(heightSource), srtmRaster, Interpolation.Method.LINEAR))
                .withComponent(RegionComponentType.COVER, new ScaledCoverRegionPopulator(new CoverTileSampler(new GlobcoverSource(globcoverRaster, "globcover")), globcoverRaster))
                .withComponent(EarthComponentTypes.OSM, new OsmRegionPopulator(
                                new OsmSampler(new OverpassSource(
                                        earthCoordinates,
                                        0.3,
                                        "osm/outline",
                                        new ResourceLocation(TerrariumEarth.MODID, "query/outline_overpass_query.oql"),
                                        1
                                ), earthCoordinates),
                                new OsmSampler(new OverpassSource(
                                        earthCoordinates,
                                        0.1,
                                        "osm/general",
                                        new ResourceLocation(TerrariumEarth.MODID, "query/general_overpass_query.oql"),
                                        2
                                ), earthCoordinates),
                                new OsmSampler(new OverpassSource(
                                        earthCoordinates,
                                        0.05,
                                        "osm/detailed",
                                        new ResourceLocation(TerrariumEarth.MODID, "query/detail_overpass_query.oql"),
                                        2
                                ), earthCoordinates)
                        )
                )
                .withAdapter(new OsmCoastlineAdapter(RegionComponentType.HEIGHT, RegionComponentType.COVER, EarthComponentTypes.OSM, earthCoordinates))
                .withAdapter(new HeightNoiseAdapter(world, RegionComponentType.HEIGHT, 2, 0.08, properties.getDouble(NOISE_SCALE)))
                .withAdapter(new HeightTransformAdapter(RegionComponentType.HEIGHT, properties.getDouble(HEIGHT_SCALE) * worldScale, heightOrigin))
                .withAdapter(new OceanDepthCorrectionAdapter(RegionComponentType.HEIGHT, properties.getInteger(OCEAN_DEPTH)))
                .withAdapter(new BeachAdapter(world, RegionComponentType.COVER, properties.getInteger(BEACH_SIZE), EarthCoverTypes.WATER, EarthCoverTypes.BEACH))
                .withAdapter(new WaterFlattenAdapter(RegionComponentType.HEIGHT, RegionComponentType.COVER, 15, EarthCoverTypes.WATER))
                .build();
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

    private Interpolation.Method selectInterpolationMethod(GenerationSettings settings) {
        double scale = 1.0 / settings.getProperties().getDouble(WORLD_SCALE);

        Interpolation.Method interpolationMethod = Interpolation.Method.CUBIC;
        if (scale >= 45.0) {
            interpolationMethod = Interpolation.Method.LINEAR;
        } else if (scale >= 20.0) {
            interpolationMethod = Interpolation.Method.COSINE;
        }

        return interpolationMethod;
    }
}
