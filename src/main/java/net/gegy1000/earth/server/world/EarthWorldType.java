package net.gegy1000.earth.server.world;

import com.google.common.collect.Lists;
import net.gegy1000.cubicglue.CubicGlue;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.earth.server.capability.EarthCapability;
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
import net.gegy1000.earth.server.world.pipeline.composer.SoilSurfaceComposer;
import net.gegy1000.earth.server.world.pipeline.composer.WaterFillSurfaceComposer;
import net.gegy1000.earth.server.world.pipeline.data.ClimateSampler;
import net.gegy1000.earth.server.world.pipeline.data.OsmSampler;
import net.gegy1000.earth.server.world.pipeline.data.OsmWaterProcessor;
import net.gegy1000.earth.server.world.pipeline.data.SoilProducer;
import net.gegy1000.earth.server.world.pipeline.data.WaterProducer;
import net.gegy1000.earth.server.world.pipeline.source.LandCoverSource;
import net.gegy1000.earth.server.world.pipeline.source.SoilCoverSource;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.gegy1000.earth.server.world.pipeline.source.WorldClimateDataset;
import net.gegy1000.earth.server.world.pipeline.source.osm.OverpassSource;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmData;
import net.gegy1000.earth.server.world.pipeline.source.tile.SoilClassificationRaster;
import net.gegy1000.earth.server.world.pipeline.source.tile.SoilRaster;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
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
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.adapter.HeightTransformAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.CoverBiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.CoverDecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.VanillaEntitySpawnComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.structure.VanillaStructureComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.BedrockSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CaveSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.CoverSurfaceDecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.HeightmapSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.function.DataMerger;
import net.gegy1000.terrarium.server.world.pipeline.data.function.InterpolationScaler;
import net.gegy1000.terrarium.server.world.pipeline.data.function.RasterSourceSampler;
import net.gegy1000.terrarium.server.world.pipeline.data.function.SlopeProducer;
import net.gegy1000.terrarium.server.world.pipeline.data.function.VoronoiScaler;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ByteRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.UnsignedByteRaster;
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

    private static final double SRTM_SCALE = EARTH_CIRCUMFERENCE / SRTM_WIDTH;
    private static final double LANDCOVER_SCALE = EARTH_CIRCUMFERENCE / LandCoverSource.GLOBAL_WIDTH;
    private static final double SOIL_SCALE = EARTH_CIRCUMFERENCE / SoilCoverSource.GLOBAL_WIDTH;
    private static final double CLIMATE_SCALE = EARTH_CIRCUMFERENCE / WorldClimateDataset.WIDTH;

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
        if (worldData == null) {
            return false;
        }
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
        private final CoordinateState landcoverRaster;
        private final CoordinateState soilRaster;
        private final CoordinateState climateRaster;

        private Initializer(World world, GenerationSettings settings) {
            this.world = world;
            this.settings = settings;

            this.worldScale = settings.getDouble(WORLD_SCALE);
            this.earthCoordinates = new LatLngCoordinateState(this.worldScale * SRTM_SCALE * 1200.0);
            this.srtmRaster = new ScaledCoordinateState(this.worldScale * SRTM_SCALE);
            this.landcoverRaster = new ScaledCoordinateState(this.worldScale * LANDCOVER_SCALE);
            this.soilRaster = new ScaledCoordinateState(this.worldScale * SOIL_SCALE);
            this.climateRaster = new ScaledCoordinateState(this.worldScale * CLIMATE_SCALE);
        }

        @Override
        public TerrariumGenerator buildGenerator(boolean preview) {
            int heightOrigin = this.settings.getInteger(HEIGHT_ORIGIN);
            List<ConstructedCover<?>> coverTypes = this.constructCoverTypes();
            BasicTerrariumGenerator.Builder builder = BasicTerrariumGenerator.builder()
                    .withSurfaceComposer(new HeightmapSurfaceComposer(RegionComponentType.HEIGHT, Blocks.STONE.getDefaultState()))
                    .withSurfaceComposer(new WaterFillSurfaceComposer(RegionComponentType.HEIGHT, EarthComponentTypes.WATER, Blocks.WATER.getDefaultState()))
                    .withSurfaceComposer(new SoilSurfaceComposer(this.world, RegionComponentType.HEIGHT, EarthComponentTypes.SOIL, Blocks.STONE.getDefaultState()))
                    .withBiomeComposer(new CoverBiomeComposer(RegionComponentType.COVER, coverTypes))
                    .withSpawnPosition(new Coordinate(this.earthCoordinates, this.settings.getDouble(SPAWN_LATITUDE), this.settings.getDouble(SPAWN_LONGITUDE)));

            if (!preview && this.settings.getBoolean(ENABLE_DECORATION)) {
                builder.withSurfaceComposer(new CoverSurfaceDecorationComposer(this.world, RegionComponentType.COVER, coverTypes));
            }

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
            InterpolationScaler scaler = this.selectScaler(this.settings);

            SrtmHeightSource heightSource = new SrtmHeightSource(this.srtmRaster, "srtm_heights");

            DataFuture<ShortRaster> heightSampler = RasterSourceSampler.sampleShort(heightSource);
            DataFuture<ShortRaster> heights = scaler.scaleShortFrom(heightSampler, this.srtmRaster);

            DataFuture<UnsignedByteRaster> slope = SlopeProducer.produce(heightSampler);
            slope = InterpolationScaler.LINEAR.scaleFrom(slope, this.srtmRaster, UnsignedByteRaster::new);

            LandCoverSource landCoverSource = new LandCoverSource(this.landcoverRaster, "landcover");
            DataFuture<CoverRaster> coverClassification = RasterSourceSampler.sample(landCoverSource, CoverRaster::new);
            coverClassification = VoronoiScaler.scaleFrom(coverClassification, this.landcoverRaster, CoverRaster::new);

            SoilCoverSource soilSource = new SoilCoverSource(this.soilRaster, "soil");
            DataFuture<SoilClassificationRaster> soilClassification = RasterSourceSampler.sample(soilSource, SoilClassificationRaster::new);
            soilClassification = VoronoiScaler.scaleFrom(soilClassification, this.soilRaster, SoilClassificationRaster::new);

            DataFuture<SoilRaster> soil = SoilProducer.produce(coverClassification, soilClassification);

            DataFuture<OsmData> osm = this.createOsmProducer();

            DataFuture<ShortRaster> waterBank = WaterProducer.produceBanks(heights, coverClassification);
            waterBank = OsmWaterProcessor.mergeOsmCoastlines(waterBank, osm, this.earthCoordinates);
//            waterBankLayer = new OsmWaterBodyLayer(waterBankLayer, osmProducer, this.earthCoordinates);

            DataFuture<WaterRaster> waterProducer = WaterProducer.produceWater(waterBank);

            ClimateSampler climateSampler = new ClimateSampler(TerrariumEarth.getClimateDataset());

            DataFuture<ShortRaster> annualRainfall = climateSampler.annualRainfall();
            annualRainfall = InterpolationScaler.LINEAR.scaleShortFrom(annualRainfall, this.climateRaster);

            DataFuture<FloatRaster> averageTemperature = climateSampler.averageTemperature();
            averageTemperature = InterpolationScaler.LINEAR.scaleFloatFrom(averageTemperature, this.climateRaster);

            BlockPos minPos = new Coordinate(this.earthCoordinates, -90.0, -180.0).toBlockPos();
            BlockPos maxPos = new Coordinate(this.earthCoordinates, 90.0, 180.0).toBlockPos();

            // TODO: Unify adapters and layers?

            return TerrariumDataProvider.builder()
                    .withComponent(RegionComponentType.HEIGHT, heights)
                    .withComponent(RegionComponentType.SLOPE, slope)
                    .withComponent(RegionComponentType.COVER, coverClassification)
                    .withComponent(EarthComponentTypes.OSM, osm)
                    .withComponent(EarthComponentTypes.WATER, waterProducer)
                    .withComponent(EarthComponentTypes.AVERAGE_TEMPERATURE, averageTemperature)
                    .withComponent(EarthComponentTypes.ANNUAL_RAINFALL, annualRainfall)
                    .withComponent(EarthComponentTypes.SOIL, soil)
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

        private DataFuture<OsmData> createOsmProducer() {
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

            Collection<DataFuture<OsmData>> samplers = sources.stream()
                    .filter(OverpassSource::shouldSample)
                    .map(source -> OsmSampler.sample(source, this.earthCoordinates))
                    .collect(Collectors.toList());

            if (!samplers.isEmpty()) {
                return DataMerger.merge(samplers);
            } else {
                return DataFuture.completed(new OsmData());
            }
        }

        private InterpolationScaler selectScaler(GenerationSettings properties) {
            double scale = 1.0 / properties.getDouble(WORLD_SCALE);
            if (scale >= 45.0) {
                return InterpolationScaler.LINEAR;
            } else if (scale >= 20.0) {
                return InterpolationScaler.COSINE;
            } else {
                return InterpolationScaler.CUBIC;
            }
        }
    }
}
