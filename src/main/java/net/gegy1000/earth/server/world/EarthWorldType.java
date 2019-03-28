package net.gegy1000.earth.server.world;

import com.google.common.collect.Lists;
import net.gegy1000.cubicglue.CubicGlue;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.earth.server.capability.EarthCapability;
import net.gegy1000.earth.server.world.coordinate.ClimateRasterCoordinateState;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.EarthComponentTypes;
import net.gegy1000.earth.server.world.pipeline.adapter.BeachAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.HeightNoiseAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.SlopeNoiseAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WaterApplyAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WaterCarveAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WaterLevelingAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WorldEdgeAdapter;
import net.gegy1000.earth.server.world.pipeline.composer.BoulderDecorationComposer;
import net.gegy1000.earth.server.world.pipeline.composer.WaterFillSurfaceComposer;
import net.gegy1000.earth.server.world.pipeline.layer.OsmCoastlineLayer;
import net.gegy1000.earth.server.world.pipeline.layer.OsmPopulatorLayer;
import net.gegy1000.earth.server.world.pipeline.layer.OsmSampleLayer;
import net.gegy1000.earth.server.world.pipeline.layer.RainfallSampleLayer;
import net.gegy1000.earth.server.world.pipeline.layer.TemperatureSampleLayer;
import net.gegy1000.earth.server.world.pipeline.layer.WaterBankPopulatorLayer;
import net.gegy1000.earth.server.world.pipeline.layer.WaterProcessorLayer;
import net.gegy1000.earth.server.world.pipeline.source.GlobcoverSource;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.gegy1000.earth.server.world.pipeline.source.WorldClimateDataset;
import net.gegy1000.earth.server.world.pipeline.source.osm.OverpassSource;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
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
import net.gegy1000.terrarium.server.world.generator.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.generator.customization.property.BooleanKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.NumberKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.widget.InversePropertyConverter;
import net.gegy1000.terrarium.server.world.generator.customization.widget.SliderWidget;
import net.gegy1000.terrarium.server.world.generator.customization.widget.ToggleWidget;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataProducerLayer;
import net.gegy1000.terrarium.server.world.pipeline.MergeDataLayer;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.adapter.HeightTransformAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.CoverBiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.CoverDecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.VanillaEntitySpawnComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.structure.VanillaStructureComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.BedrockSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CaveSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CoverSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.HeightmapSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.layer.CoverTileSampleLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ScaledCoverLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ScaledFloatLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ScaledShortLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ScaledUnsignedByteLater;
import net.gegy1000.terrarium.server.world.pipeline.layer.ShortTileSampleLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.SlopeProducerLayer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.FloatRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EarthWorldType extends TerrariumWorldType {
    private static final double EARTH_CIRCUMFERENCE = 40075000.0;

    private static final double SRTM_WIDTH = 1200.0 * 360.0;
    private static final double GLOB_WIDTH = 360.0 * 360.0;
    private static final double CLIMATE_WIDTH = WorldClimateDataset.WIDTH;

    private static final double SRTM_SCALE = EARTH_CIRCUMFERENCE / SRTM_WIDTH;
    private static final double GLOB_SCALE = EARTH_CIRCUMFERENCE / GLOB_WIDTH;
    private static final double CLIMATE_SCALE = EARTH_CIRCUMFERENCE / CLIMATE_WIDTH;

    private static final int HIGHEST_POINT_METERS = 8900;

    private static final ResourceLocation IDENTIFIER = new ResourceLocation(TerrariumEarth.MODID, "earth_generator");
    private static final ResourceLocation PRESET = new ResourceLocation(TerrariumEarth.MODID, "earth_default");

    public static final PropertyKey<Number> SPAWN_LATITUDE = new NumberKey("spawn_latitude");
    public static final PropertyKey<Number> SPAWN_LONGITUDE = new NumberKey("spawn_longitude");
    public static final PropertyKey<Boolean> ENABLE_DECORATION = new BooleanKey("enable_decoration");
    public static final PropertyKey<Number> WORLD_SCALE = new NumberKey("world_scale");
    public static final PropertyKey<Number> HEIGHT_SCALE = new NumberKey("height_scale");
    public static final PropertyKey<Number> NOISE_SCALE = new NumberKey("noise_scale");
    public static final PropertyKey<Number> HEIGHT_ORIGIN = new NumberKey("height_origin");
    public static final PropertyKey<Number> OCEAN_DEPTH = new NumberKey("ocean_depth");
    public static final PropertyKey<Number> BEACH_SIZE = new NumberKey("beach_size");
    public static final PropertyKey<Boolean> ENABLE_BUILDINGS = new BooleanKey("enable_buildings");
    public static final PropertyKey<Boolean> ENABLE_STREETS = new BooleanKey("enable_streets");

    public static final PropertyKey<Boolean> CAVE_GENERATION = new BooleanKey("cave_generation");

    public EarthWorldType() {
        super("earth", IDENTIFIER, PRESET);
    }

    @Override
    public TerrariumGeneratorInitializer createInitializer(World world, GenerationSettings settings) {
        world.setSeaLevel(settings.getInteger(HEIGHT_ORIGIN));
        return new Initializer(world, settings);
    }

    @Override
    public Collection<ICapabilityProvider> createCapabilities(World world, GenerationSettings settings) {
        CoordinateState earthCoordinates = new LatLngCoordinateState(settings.getDouble(WORLD_SCALE) * SRTM_SCALE * 1200.0);
        return Lists.newArrayList(new EarthCapability.Impl(earthCoordinates));
    }

    @Override
    public PropertyPrototype buildPropertyPrototype() {
        return PropertyPrototype.builder()
                .withProperties(SPAWN_LATITUDE, SPAWN_LONGITUDE)
                .withProperties(WORLD_SCALE, HEIGHT_SCALE, NOISE_SCALE)
                .withProperties(OCEAN_DEPTH, HEIGHT_ORIGIN)
                .withProperties(BEACH_SIZE)
                .withProperties(ENABLE_DECORATION, ENABLE_BUILDINGS, ENABLE_STREETS)
                .withProperties(CAVE_GENERATION)
                .build();
    }

    @Override
    public TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.builder()
                .withCategory("world",
                        new SliderWidget(WORLD_SCALE, 1.0, 200.0, 5.0, 1.0, new InversePropertyConverter()),
                        new SliderWidget(HEIGHT_SCALE, 0.0, 10.0, 0.5, 0.1),
                        new SliderWidget(NOISE_SCALE, 0.0, 3.0, 0.5, 0.1),
                        new SliderWidget(OCEAN_DEPTH, 0, 32, 1, 1),
                        new SliderWidget(HEIGHT_ORIGIN, -63, 128, 1, 1),
                        new SliderWidget(BEACH_SIZE, 0, 8, 1, 1)
                )
                .withCategory("features",
                        new ToggleWidget(ENABLE_DECORATION),
                        new ToggleWidget(ENABLE_BUILDINGS).locked(),
                        new ToggleWidget(ENABLE_STREETS).locked()
                )
                .withCategory("procedural_features",
                        new ToggleWidget(CAVE_GENERATION)
                )
                .build();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected TerrariumCustomizationGui createCustomizationGui(GuiCreateWorld parent, WorldType worldType, TerrariumPreset preset) {
        return new EarthCustomizationGui(parent, worldType, this, preset);
    }

    @Override
    public boolean shouldReduceSlimes(World world, Random random) {
        TerrariumWorldData worldData = TerrariumWorldData.get(world);
        return worldData.getSettings().getInteger(HEIGHT_ORIGIN) < 40;
    }

    @Override
    protected int calculateMaxGenerationHeight(WorldServer world, GenerationSettings settings) {
        double globalScale = settings.getDouble(HEIGHT_SCALE) * settings.getDouble(WORLD_SCALE);
        double highestPointBlocks = HIGHEST_POINT_METERS * globalScale;
        return MathHelper.ceil(highestPointBlocks + settings.getDouble(HEIGHT_ORIGIN) + 1);
    }

    private static class Initializer implements TerrariumGeneratorInitializer {
        private final World world;
        private final GenerationSettings settings;

        private final double worldScale;

        private final CoordinateState earthCoordinates;
        private final CoordinateState srtmRaster;
        private final CoordinateState globcoverRaster;
        private final CoordinateState climateRaster;

        private Initializer(World world, GenerationSettings settings) {
            this.world = world;
            this.settings = settings;

            this.worldScale = settings.getDouble(WORLD_SCALE);
            this.earthCoordinates = new LatLngCoordinateState(this.worldScale * SRTM_SCALE * 1200.0);
            this.srtmRaster = new ScaledCoordinateState(this.worldScale * SRTM_SCALE);
            this.globcoverRaster = new ScaledCoordinateState(this.worldScale * GLOB_SCALE);
            this.climateRaster = new ClimateRasterCoordinateState(this.worldScale * CLIMATE_SCALE);
        }

        @Override
        public TerrariumGenerator buildGenerator(boolean preview) {
            int heightOrigin = this.settings.getInteger(HEIGHT_ORIGIN);
            List<ConstructedCover<?>> coverTypes = this.constructCoverTypes();
            BasicTerrariumGenerator.Builder builder = BasicTerrariumGenerator.builder()
                    .withSurfaceComposer(new HeightmapSurfaceComposer(RegionComponentType.HEIGHT, Blocks.STONE.getDefaultState()))
                    .withSurfaceComposer(new WaterFillSurfaceComposer(RegionComponentType.HEIGHT, EarthComponentTypes.WATER, Blocks.WATER.getDefaultState()))
                    .withSurfaceComposer(new CoverSurfaceComposer(this.world, RegionComponentType.HEIGHT, RegionComponentType.COVER, coverTypes, !preview && this.settings.getBoolean(ENABLE_DECORATION), Blocks.STONE.getDefaultState()))
                    .withBiomeComposer(new CoverBiomeComposer(RegionComponentType.COVER, coverTypes))
                    .withSpawnPosition(new Coordinate(this.earthCoordinates, this.settings.getDouble(SPAWN_LATITUDE), this.settings.getDouble(SPAWN_LONGITUDE)));

            if (!CubicGlue.isCubic(this.world)) {
                builder.withSurfaceComposer(new BedrockSurfaceComposer(this.world, Blocks.BEDROCK.getDefaultState(), Math.min(heightOrigin - 1, 5)));
            }

            if (this.settings.getBoolean(ENABLE_DECORATION)) {
                builder.withDecorationComposer(new CoverDecorationComposer(this.world, RegionComponentType.COVER, coverTypes));
                builder.withDecorationComposer(new BoulderDecorationComposer(this.world, RegionComponentType.SLOPE));
            }

            this.attachIntegrationComposers(preview, builder);

            return builder.build();
        }

        private void attachIntegrationComposers(boolean preview, BasicTerrariumGenerator.Builder builder) {
            if (!preview) {
                if (this.settings.get(CAVE_GENERATION)) {
                    builder.withSurfaceComposer(new CaveSurfaceComposer(this.world));
                }

                builder.withStructureComposer(new VanillaStructureComposer(this.world));
            }

            builder.withDecorationComposer(new VanillaEntitySpawnComposer(this.world));
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
            int heightOrigin = this.settings.getInteger(HEIGHT_ORIGIN);
            SrtmHeightSource heightSource = new SrtmHeightSource(this.srtmRaster, "srtm_heights");
            DataLayer<ShortRasterTile> heightSampler = new ShortTileSampleLayer(heightSource);

            DataLayer<ShortRasterTile> heightProducer = this.createHeightProducer(heightSampler);
            DataLayer<CoverRasterTile> coverProducer = this.createCoverProducer();
            DataLayer<OsmTile> osmProducer = this.createOsmProducer();

            DataLayer<ShortRasterTile> waterBankLayer = new WaterBankPopulatorLayer(coverProducer, heightProducer);
            waterBankLayer = new OsmCoastlineLayer(waterBankLayer, osmProducer, this.earthCoordinates);
//            waterBankLayer = new OsmWaterBodyLayer(waterBankLayer, osmProducer, this.earthCoordinates);

            DataLayer<WaterRasterTile> waterProducer = new WaterProcessorLayer(waterBankLayer);

            DataLayer<ShortRasterTile> annualRainfallSampler = new ScaledShortLayer(new RainfallSampleLayer(), this.climateRaster, Interpolation.Method.LINEAR);
            DataLayer<FloatRasterTile> averageTemperatureSampler = new ScaledFloatLayer(new TemperatureSampleLayer(), this.climateRaster, Interpolation.Method.LINEAR);

            BlockPos minPos = new Coordinate(this.earthCoordinates, -90.0, -180.0).toBlockPos();
            BlockPos maxPos = new Coordinate(this.earthCoordinates, 90.0, 180.0).toBlockPos();

            // TODO: Rather than constructing a data provider that dictates data flow, let's rather
            //  have data flow be specified explicitly through a function

            return TerrariumDataProvider.builder()
                    .withComponent(RegionComponentType.HEIGHT, heightProducer)
                    .withComponent(RegionComponentType.SLOPE, this.createSlopeProducer(heightSampler))
                    .withComponent(RegionComponentType.COVER, coverProducer)
                    .withComponent(EarthComponentTypes.OSM, osmProducer)
                    .withComponent(EarthComponentTypes.WATER, waterProducer)
                    .withComponent(EarthComponentTypes.AVERAGE_TEMPERATURE, averageTemperatureSampler)
                    .withComponent(EarthComponentTypes.ANNUAL_RAINFALL, annualRainfallSampler)
//                    .withAdapter(new OsmAreaCoverAdapter(this.earthCoordinates, EarthComponentTypes.OSM, RegionComponentType.COVER))
                    .withAdapter(new WaterApplyAdapter(this.earthCoordinates, EarthComponentTypes.WATER, RegionComponentType.HEIGHT, RegionComponentType.COVER))
                    .withAdapter(new HeightNoiseAdapter(this.world, RegionComponentType.HEIGHT, EarthComponentTypes.WATER, 2, 0.04, this.settings.getDouble(NOISE_SCALE)))
                    .withAdapter(new HeightTransformAdapter(RegionComponentType.HEIGHT, this.settings.getDouble(HEIGHT_SCALE) * this.worldScale, heightOrigin))
                    .withAdapter(new WaterLevelingAdapter(EarthComponentTypes.WATER, RegionComponentType.HEIGHT, heightOrigin + 1))
                    .withAdapter(new WaterCarveAdapter(EarthComponentTypes.WATER, RegionComponentType.HEIGHT, this.settings.getInteger(OCEAN_DEPTH)))
                    .withAdapter(new SlopeNoiseAdapter(this.world, RegionComponentType.SLOPE, this.settings.getDouble(NOISE_SCALE)))
//                    .withAdapter(new OceanDepthCorrectionAdapter(RegionComponentType.HEIGHT, this.properties.getInteger(OCEAN_DEPTH)))
                    .withAdapter(new BeachAdapter(this.world, RegionComponentType.COVER, EarthComponentTypes.WATER, this.settings.getInteger(BEACH_SIZE), EarthCoverTypes.BEACH))
                    .withAdapter(new WorldEdgeAdapter(RegionComponentType.HEIGHT, RegionComponentType.COVER, this.settings.getInteger(HEIGHT_ORIGIN), minPos, maxPos))
//                    .withAdapter(new WaterFlattenAdapter(RegionComponentType.HEIGHT, RegionComponentType.COVER, 15, EarthCoverTypes.WATER))
                    .build();
        }

        private DataLayer<ShortRasterTile> createHeightProducer(DataLayer<ShortRasterTile> heightSampler) {
            Interpolation.Method interpolationMethod = this.selectInterpolationMethod(this.settings);
            return new ScaledShortLayer(heightSampler, this.srtmRaster, interpolationMethod);
        }

        private DataLayer<UnsignedByteRasterTile> createSlopeProducer(DataLayer<ShortRasterTile> heightSampler) {
            DataLayer<UnsignedByteRasterTile> layer = new SlopeProducerLayer(heightSampler);
            layer = new ScaledUnsignedByteLater(layer, this.srtmRaster, Interpolation.Method.LINEAR);
            return layer;
        }

        private DataLayer<CoverRasterTile> createCoverProducer() {
            GlobcoverSource globcoverSource = new GlobcoverSource(this.globcoverRaster, "globcover");
            DataLayer<CoverRasterTile> layer = new CoverTileSampleLayer(globcoverSource);
            layer = new ScaledCoverLayer(layer, this.globcoverRaster);
            return layer;
        }

        private DataLayer<OsmTile> createOsmProducer() {
            List<OverpassSource> sources = new ArrayList<>();

            sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.3,
                    "osm/coastline",
                    new ResourceLocation(TerrariumEarth.MODID, "query/coastline_overpass_query.oql"),
                    1
            ));
            /*sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.3,
                    "osm/outline",
                    new ResourceLocation(TerrariumEarth.MODID, "query/outline_overpass_query.oql"),
                    12
            ));
            sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.15,
                    "osm/natural",
                    new ResourceLocation(TerrariumEarth.MODID, "query/natural_overpass_query.oql"),
                    1
            ));
            sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.1,
                    "osm/general",
                    new ResourceLocation(TerrariumEarth.MODID, "query/general_overpass_query.oql"),
                    6
            ));
            sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.05,
                    "osm/detailed",
                    new ResourceLocation(TerrariumEarth.MODID, "query/detail_overpass_query.oql"),
                    4
            ));*/

            List<DataLayer<OsmTile>> samplers = sources.stream()
                    .filter(OverpassSource::shouldSample)
                    .map(overpassSource -> new OsmSampleLayer(overpassSource, this.earthCoordinates))
                    .collect(Collectors.toList());

            if (!samplers.isEmpty()) {
                DataLayer<OsmTile> layer = new MergeDataLayer<>(samplers);
                layer = new OsmPopulatorLayer(layer);
                return layer;
            }

            return (DataProducerLayer<OsmTile>) (context, view) -> new OsmTile();
        }

        private Interpolation.Method selectInterpolationMethod(GenerationSettings properties) {
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
