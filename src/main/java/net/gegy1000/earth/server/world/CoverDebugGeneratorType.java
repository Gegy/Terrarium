package net.gegy1000.earth.server.world;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.pipeline.composer.EarthBiomeComposer;
import net.gegy1000.earth.server.world.pipeline.layer.DebugCoverPopulator;
import net.gegy1000.terrarium.server.world.GenerationContext;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.chunk.ComposableBiomeSource;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.customization.TerrariumCustomization;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.BasicCompositionProcedure;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.BedrockComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.BiomeSurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.HeightmapTerrainComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.BiomeFeatureComposer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ConstantShortProducer;
import net.gegy1000.terrarium.server.world.pipeline.layer.ConstantUnsignedBytePopulator;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Random;

public class CoverDebugGeneratorType extends TerrariumGeneratorType<TerrariumGeneratorConfig> {
    private static final Identifier IDENTIFIER = new Identifier(TerrariumEarth.MODID, "debug_generator");
    private static final Identifier PRESET = new Identifier(TerrariumEarth.MODID, "debug_default");

    public CoverDebugGeneratorType() {
        super("earth_debug", IDENTIFIER, PRESET);
        this.hidden = true;
    }

    @Override
    public ChunkGenerator<TerrariumGeneratorConfig> createGenerator(World world, GenerationSettings settings, GenerationContext context) {
        Initializer initializer = new Initializer(world, settings);
        TerrariumGeneratorConfig config = initializer.buildConfig();

        BiomeSource biomeSource = new ComposableBiomeSource<>(config);
        return new ComposableChunkGenerator<>(world, biomeSource, config);
    }

    @Override
    public PropertyPrototype buildPropertyPrototype() {
        return PropertyPrototype.EMPTY;
    }

    @Override
    protected TerrariumCustomization buildCustomization() {
        return TerrariumCustomization.EMPTY;
    }

    @Override
    public boolean shouldReduceSlimeSpawns(IWorld world, Random random) {
        return true;
    }

    private static class Initializer {
        private final World world;
        private final GenerationSettings settings;

        private Initializer(World world, GenerationSettings settings) {
            this.world = world;
            this.settings = settings;
        }

        public TerrariumGeneratorConfig buildConfig() {
            ChunkCompositionProcedure<?> procedure = this.buildCompositionProcedure();
            TerrariumDataProvider dataProvider = this.buildDataProvider();
            Coordinate spawnPosition = Coordinate.fromBlock(0.0, 0.0);

            RegionGenerationHandler regionHandler = new RegionGenerationHandler(dataProvider);

            return new TerrariumGeneratorConfig(this.settings, regionHandler, procedure, spawnPosition);
        }

        public ChunkCompositionProcedure<?> buildCompositionProcedure() {
            return BasicCompositionProcedure.builder()
                    .withNoiseComposer(new HeightmapTerrainComposer<>(RegionComponentType.HEIGHT, Blocks.QUARTZ_BLOCK.getDefaultState()))
                    .withComposer(ChunkStatus.NOISE, new BedrockComposer<>(Blocks.BEDROCK.getDefaultState(), 0))
                    .withComposer(ChunkStatus.SURFACE, new BiomeSurfaceComposer<>(this.world, RegionComponentType.BIOME))
                    .withDecorationComposer(ChunkStatus.FEATURES, new BiomeFeatureComposer<>(RegionComponentType.BIOME))
                    .withBiomeComposer(new EarthBiomeComposer(RegionComponentType.BIOME))
                    .build();
        }

        public TerrariumDataProvider buildDataProvider() {
            return TerrariumDataProvider.builder()
                    .withComponent(RegionComponentType.HEIGHT, new ConstantShortProducer((short) 62))
                    .withComponent(RegionComponentType.SLOPE, new ConstantUnsignedBytePopulator(0))
                    .withComponent(RegionComponentType.BIOME, new DebugCoverPopulator())
                    .build();
        }
    }
}
