package net.gegy1000.earth.server.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.EarthCustomizationGui;
import net.gegy1000.earth.server.world.chunk.EarthChunkGenerator;
import net.gegy1000.earth.server.world.cover.EarthCoverBiomes;
import net.gegy1000.earth.server.world.pipeline.EarthComponentTypes;
import net.gegy1000.earth.server.world.pipeline.adapter.BeachAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.HeightNoiseAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.OsmAreaCoverAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WaterApplyAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WaterCarveAdapter;
import net.gegy1000.earth.server.world.pipeline.adapter.WaterLevelingAdapter;
import net.gegy1000.earth.server.world.pipeline.composer.EarthBiomeComposer;
import net.gegy1000.earth.server.world.pipeline.composer.WaterFillComposer;
import net.gegy1000.earth.server.world.pipeline.layer.OsmCoastlineLayer;
import net.gegy1000.earth.server.world.pipeline.layer.OsmPopulatorLayer;
import net.gegy1000.earth.server.world.pipeline.layer.OsmSampleLayer;
import net.gegy1000.earth.server.world.pipeline.layer.OsmWaterBodyLayer;
import net.gegy1000.earth.server.world.pipeline.layer.WaterBankPopulatorLayer;
import net.gegy1000.earth.server.world.pipeline.layer.WaterProcessorLayer;
import net.gegy1000.earth.server.world.pipeline.source.GlobcoverSource;
import net.gegy1000.earth.server.world.pipeline.source.SrtmHeightSource;
import net.gegy1000.earth.server.world.pipeline.source.osm.OverpassSource;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.GenerationContext;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.chunk.ComposableBiomeSource;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.LatLngCoordinateState;
import net.gegy1000.terrarium.server.world.coordinate.ScaledCoordinateState;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.customization.property.BooleanKey;
import net.gegy1000.terrarium.server.world.customization.property.NumberKey;
import net.gegy1000.terrarium.server.world.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.customization.widget.InversePropertyConverter;
import net.gegy1000.terrarium.server.world.customization.widget.SliderWidget;
import net.gegy1000.terrarium.server.world.customization.widget.ToggleWidget;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataProducerLayer;
import net.gegy1000.terrarium.server.world.pipeline.MergeDataLayer;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.adapter.HeightTransformAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.BasicCompositionProcedure;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.BedrockComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.BiomeCarverComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.BiomeSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.HeightmapTerrainComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.BiomeFeatureComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.BiomeSpawnComposer;
import net.gegy1000.terrarium.server.world.pipeline.layer.CoverTileSampleLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ScaledCoverLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ScaledShortLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ScaledUnsignedByteLater;
import net.gegy1000.terrarium.server.world.pipeline.layer.ShortTileSampleLayer;
import net.gegy1000.terrarium.server.world.pipeline.layer.SlopeProducerLayer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.util.Identifier;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EarthGeneratorType extends TerrariumGeneratorType<EarthGeneratorConfig> {
    private static final double EARTH_CIRCUMFERENCE = 40075000.0;
    private static final double SRTM_WIDTH = 1200.0 * 360.0;
    private static final double SRTM_SCALE = EARTH_CIRCUMFERENCE / SRTM_WIDTH;
    private static final double GLOB_RATIO = 10.0 / 3.0;

    private static final Identifier IDENTIFIER = new Identifier(TerrariumEarth.MODID, "earth_generator");
    private static final Identifier PRESET = new Identifier(TerrariumEarth.MODID, "earth_default");

    public static final PropertyKey<Number> SPAWN_LATITUDE = new NumberKey("spawn_latitude");
    public static final PropertyKey<Number> SPAWN_LONGITUDE = new NumberKey("spawn_longitude");
    public static final PropertyKey<Boolean> ENABLE_DECORATION = new BooleanKey("enable_decoration");
    public static final PropertyKey<Boolean> ENABLE_DEFAULT_DECORATION = new BooleanKey("enable_default_decoration");
    public static final PropertyKey<Number> WORLD_SCALE = new NumberKey("world_scale");
    public static final PropertyKey<Number> HEIGHT_SCALE = new NumberKey("height_scale");
    public static final PropertyKey<Number> NOISE_SCALE = new NumberKey("noise_scale");
    public static final PropertyKey<Number> HEIGHT_ORIGIN = new NumberKey("height_origin");
    public static final PropertyKey<Number> OCEAN_DEPTH = new NumberKey("ocean_depth");
    public static final PropertyKey<Number> BEACH_SIZE = new NumberKey("beach_size");
    public static final PropertyKey<Boolean> ENABLE_BUILDINGS = new BooleanKey("enable_buildings");
    public static final PropertyKey<Boolean> ENABLE_STREETS = new BooleanKey("enable_streets");
    public static final PropertyKey<Boolean> ENABLE_DEFAULT_FEATURES = new BooleanKey("enable_default_features");
    public static final PropertyKey<Boolean> ENABLE_CAVE_GENERATION = new BooleanKey("enable_cave_generation");
    public static final PropertyKey<Boolean> ENABLE_RESOURCE_GENERATION = new BooleanKey("enable_resource_generation");
    public static final PropertyKey<Boolean> ENABLE_LAKE_GENERATION = new BooleanKey("enable_lake_generation");
    public static final PropertyKey<Boolean> ENABLE_LAVA_GENERATION = new BooleanKey("enable_lava_generation");
    public static final PropertyKey<Boolean> ENABLE_MOD_GENERATION = new BooleanKey("enable_mod_generation");

    public EarthGeneratorType() {
        super("earth", IDENTIFIER, PRESET);
    }

    @Override
    public ChunkGenerator<EarthGeneratorConfig> createGenerator(World world, GenerationSettings settings, GenerationContext context) {
        // TODO: Sea level
//        world.setSeaLevel(settings.getInteger(HEIGHT_ORIGIN));

        Initializer initializer = new Initializer(world, settings, context);
        EarthGeneratorConfig config = initializer.buildConfig();

        BiomeSource biomeSource = new ComposableBiomeSource<>(config);
        return new EarthChunkGenerator(world, biomeSource, config);
    }

    @Override
    public PropertyPrototype buildPropertyPrototype() {
        return PropertyPrototype.builder()
                .withProperties(SPAWN_LATITUDE, SPAWN_LONGITUDE)
                .withProperties(WORLD_SCALE, HEIGHT_SCALE, NOISE_SCALE)
                .withProperties(OCEAN_DEPTH, HEIGHT_ORIGIN)
                .withProperties(BEACH_SIZE)
                .withProperties(ENABLE_DECORATION, ENABLE_BUILDINGS, ENABLE_STREETS)
                .withProperties(ENABLE_DEFAULT_DECORATION, ENABLE_DEFAULT_FEATURES)
                .withProperties(ENABLE_MOD_GENERATION, ENABLE_CAVE_GENERATION, ENABLE_RESOURCE_GENERATION)
                .withProperties(ENABLE_LAKE_GENERATION, ENABLE_LAVA_GENERATION)
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
                        new SliderWidget(HEIGHT_ORIGIN, -63, 128, 1, 1),
                        new SliderWidget(BEACH_SIZE, 0, 8, 1, 1)
                )
                .withCategory("features",
                        new ToggleWidget(ENABLE_DECORATION),
                        new ToggleWidget(ENABLE_BUILDINGS).locked(),
                        new ToggleWidget(ENABLE_STREETS).locked()
                )
                .withCategory("survival",
                        new ToggleWidget(ENABLE_DEFAULT_DECORATION),
                        new ToggleWidget(ENABLE_DEFAULT_FEATURES),
                        new ToggleWidget(ENABLE_MOD_GENERATION),
                        new ToggleWidget(ENABLE_CAVE_GENERATION),
                        new ToggleWidget(ENABLE_RESOURCE_GENERATION),
                        new ToggleWidget(ENABLE_LAKE_GENERATION),
                        new ToggleWidget(ENABLE_LAVA_GENERATION)
                )
                .build();
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected TerrariumCustomizationGui createCustomizationGui(NewLevelGui parent, TerrariumPreset preset) {
        return new EarthCustomizationGui(parent, this, preset);
    }

    @Override
    public boolean shouldReduceSlimeSpawns(IWorld world, Random random) {
        TerrariumGeneratorConfig config = TerrariumGeneratorConfig.get(world);
        if (config == null) {
            return false;
        }
        return config.getSettings().getInteger(HEIGHT_ORIGIN) < 40;
    }

    private static class Initializer {
        private final World world;
        private final GenerationSettings settings;
        private final GenerationContext context;

        private final double worldScale;

        private final CoordinateState earthCoordinates;
        private final CoordinateState srtmRaster;
        private final CoordinateState globcoverRaster;

        private Initializer(World world, GenerationSettings settings, GenerationContext context) {
            this.world = world;
            this.settings = settings;
            this.context = context;

            this.worldScale = settings.getDouble(WORLD_SCALE);
            this.earthCoordinates = new LatLngCoordinateState(this.worldScale * SRTM_SCALE * 1200.0);
            this.srtmRaster = new ScaledCoordinateState(this.worldScale * SRTM_SCALE);
            this.globcoverRaster = new ScaledCoordinateState(this.worldScale * SRTM_SCALE * GLOB_RATIO);
        }

        public EarthGeneratorConfig buildConfig() {
            ChunkCompositionProcedure<?> procedure = this.buildCompositionProcedure();
            TerrariumDataProvider dataProvider = this.buildDataProvider();
            Coordinate spawnPosition = this.createSpawnPoint();

            RegionGenerationHandler regionHandler = new RegionGenerationHandler(dataProvider);

            return new EarthGeneratorConfig(this.settings, regionHandler, procedure, spawnPosition, this.earthCoordinates);
        }

        public Coordinate createSpawnPoint() {
            double latitude = this.settings.getDouble(SPAWN_LATITUDE);
            double longitude = this.settings.getDouble(SPAWN_LONGITUDE);
            return new Coordinate(this.earthCoordinates, latitude, longitude);
        }

        public ChunkCompositionProcedure<?> buildCompositionProcedure() {
            int heightOrigin = this.settings.getInteger(HEIGHT_ORIGIN);
            BasicCompositionProcedure.Builder<?> builder = BasicCompositionProcedure.builder()
                    .withNoiseComposer(new HeightmapTerrainComposer<>(RegionComponentType.HEIGHT, Blocks.STONE.getDefaultState()))
                    .withComposer(ChunkStatus.NOISE, new BedrockComposer<>(Blocks.BEDROCK.getDefaultState(), Math.min(heightOrigin - 1, 5)))
                    .withComposer(ChunkStatus.NOISE, new WaterFillComposer<>(RegionComponentType.HEIGHT, EarthComponentTypes.WATER, Blocks.WATER.getDefaultState()))
                    .withComposer(ChunkStatus.SURFACE, new BiomeSurfaceComposer<>(this.world, RegionComponentType.BIOME))
                    .withComposer(ChunkStatus.CARVERS, new BiomeCarverComposer<>(RegionComponentType.BIOME, GenerationStep.Carver.AIR))
                    .withComposer(ChunkStatus.LIQUID_CARVERS, new BiomeCarverComposer<>(RegionComponentType.BIOME, GenerationStep.Carver.LIQUID))
                    .withDecorationComposer(ChunkStatus.FEATURES, new BiomeFeatureComposer<>(RegionComponentType.BIOME))
                    .withDecorationComposer(ChunkStatus.SPAWN, new BiomeSpawnComposer<>(RegionComponentType.BIOME))
                    .withBiomeComposer(new EarthBiomeComposer(RegionComponentType.BIOME));

            if (this.context == GenerationContext.WORLD) {

            }
            // TODO: we should have a config to tweak specific features
//            if (!preview) {
//                if (this.settings.getBoolean(ENABLE_CAVE_GENERATION)) {
//                    builder.withComposer(new CaveComposer(this.world));
//                }
//                if (this.settings.getBoolean(ENABLE_DEFAULT_FEATURES)) {
//                    builder.withStructureComposer(new VanillaStructureComposer(this.world, this.chunkGenerator));
//                }
//            }
//
//            if (this.settings.getBoolean(ENABLE_DECORATION)) {
//                builder.withDecorationComposer(new BiomeDecorationComposer(this.world, RegionComponentType.BIOME, biomes));
//                builder.withDecorationComposer(new BoulderDecorationComposer(this.world, RegionComponentType.SLOPE));
//            }
//            if (this.settings.getBoolean(ENABLE_DEFAULT_DECORATION)) {
//                builder.withDecorationComposer(new BiomeSpawnComposer(this.world));
//            }
//            if (this.settings.getBoolean(ENABLE_LAKE_GENERATION)) {
//                builder.withDecorationComposer(new LakeDecorationComposer(this.world));
//            }
//            if (this.settings.getBoolean(ENABLE_LAVA_GENERATION)) {
//                builder.withDecorationComposer(new LavaLakeDecorationComposer(this.world));
//            }
//            if (this.settings.getBoolean(ENABLE_RESOURCE_GENERATION)) {
//                builder.withDecorationComposer(new VanillaOreDecorationComposer(this.world));
//            }
//            if (this.settings.getBoolean(ENABLE_MOD_GENERATION)) {
//                builder.withDecorationComposer(new ModdedDecorationComposer(this.world));
//            }

            return builder.build();
        }

        public TerrariumDataProvider buildDataProvider() {
            int heightOrigin = this.settings.getInteger(HEIGHT_ORIGIN);
            SrtmHeightSource heightSource = new SrtmHeightSource(this.srtmRaster, "srtm_heights");
            DataLayer<ShortRasterTile> heightSampler = new ShortTileSampleLayer(heightSource);

            DataLayer<ShortRasterTile> heightProducer = this.createHeightProducer(heightSampler);
            DataLayer<BiomeRasterTile> coverProducer = this.createCoverProducer();
            DataLayer<OsmTile> osmProducer = this.createOsmProducer();

            DataLayer<ShortRasterTile> waterBankLayer = new WaterBankPopulatorLayer(coverProducer, heightProducer);
            waterBankLayer = new OsmCoastlineLayer(waterBankLayer, osmProducer, this.earthCoordinates);
            waterBankLayer = new OsmWaterBodyLayer(waterBankLayer, osmProducer, this.earthCoordinates);

            DataLayer<WaterRasterTile> waterProducer = new WaterProcessorLayer(waterBankLayer);

            return TerrariumDataProvider.builder()
                    .withComponent(RegionComponentType.HEIGHT, heightProducer)
                    .withComponent(RegionComponentType.SLOPE, this.createSlopeProducer(heightSampler))
                    .withComponent(RegionComponentType.BIOME, coverProducer)
                    .withComponent(EarthComponentTypes.OSM, osmProducer)
                    .withComponent(EarthComponentTypes.WATER, waterProducer)
                    .withAdapter(new OsmAreaCoverAdapter(this.earthCoordinates, EarthComponentTypes.OSM, RegionComponentType.BIOME))
                    .withAdapter(new WaterApplyAdapter(this.earthCoordinates, EarthComponentTypes.WATER, RegionComponentType.HEIGHT, RegionComponentType.BIOME))
                    .withAdapter(new HeightNoiseAdapter(this.world, RegionComponentType.HEIGHT, EarthComponentTypes.WATER, 2, 0.04, this.settings.getDouble(NOISE_SCALE)))
                    .withAdapter(new HeightTransformAdapter(this.world, RegionComponentType.HEIGHT, this.settings.getDouble(HEIGHT_SCALE) * this.worldScale, heightOrigin))
                    .withAdapter(new WaterLevelingAdapter(EarthComponentTypes.WATER, RegionComponentType.HEIGHT, heightOrigin + 1))
                    .withAdapter(new WaterCarveAdapter(EarthComponentTypes.WATER, RegionComponentType.HEIGHT, this.settings.getInteger(OCEAN_DEPTH)))
//                    .withAdapter(new OceanDepthCorrectionAdapter(RegionComponentType.HEIGHT, this.properties.getInteger(OCEAN_DEPTH)))
                    .withAdapter(new BeachAdapter(this.world, RegionComponentType.BIOME, EarthComponentTypes.WATER, this.settings.getInteger(BEACH_SIZE), EarthCoverBiomes.BEACH))
//                    .withAdapter(new WaterFlattenAdapter(RegionComponentType.HEIGHT, RegionComponentType.COVER, 15, EarthCoverBiomes.WATER))
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

        private DataLayer<BiomeRasterTile> createCoverProducer() {
            GlobcoverSource globcoverSource = new GlobcoverSource(this.globcoverRaster, "globcover");
            DataLayer<BiomeRasterTile> layer = new CoverTileSampleLayer(globcoverSource);
            layer = new ScaledCoverLayer(layer, this.globcoverRaster);
            return layer;
        }

        private DataLayer<OsmTile> createOsmProducer() {
            List<OverpassSource> sources = new ArrayList<>();

            sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.3,
                    "osm/outline",
                    new Identifier(TerrariumEarth.MODID, "query/outline_overpass_query.oql"),
                    12
            ));
            sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.15,
                    "osm/natural",
                    new Identifier(TerrariumEarth.MODID, "query/natural_overpass_query.oql"),
                    1
            ));
            /*sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.1,
                    "osm/general",
                    new Identifier(TerrariumEarth.MODID, "query/general_overpass_query.oql"),
                    6
            ));
            sources.add(new OverpassSource(
                    this.earthCoordinates,
                    0.05,
                    "osm/detailed",
                    new Identifier(TerrariumEarth.MODID, "query/detail_overpass_query.oql"),
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
