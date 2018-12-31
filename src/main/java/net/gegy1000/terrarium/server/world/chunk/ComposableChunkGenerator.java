package net.gegy1000.terrarium.server.world.chunk;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.class_3233;
import net.minecraft.sortme.StructureManager;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class ComposableChunkGenerator<C extends TerrariumGeneratorConfig> extends TerrariumChunkGenerator<C> {
    private final RegionGenerationHandler regionHandler;
    private final ChunkCompositionProcedure<C> compositionProcedure;

    public ComposableChunkGenerator(IWorld world, BiomeSource biomeSource, C config) {
        super(world, biomeSource, config);

        this.regionHandler = config.getRegionHandler();
        this.compositionProcedure = config.getCompositionProcedure();
    }

    @Override
    public void populateBiomes(Chunk chunk) {
        super.populateBiomes(chunk);
        this.invokeComposer(ChunkStatus.BIOMES, chunk);
    }

    @Override
    public void method_16129(Chunk chunk, ChunkGenerator<?> generator, StructureManager structureManager) {
        this.invokeComposer(ChunkStatus.STRUCTURE_STARTS, chunk);
    }

    @Override
    public void method_16130(IWorld world, Chunk chunk) {
        this.invokeComposer(ChunkStatus.STRUCTURE_REFERENCES, chunk);
    }

    @Override
    public void populateNoise(IWorld world, Chunk chunk) {
        this.invokeComposer(ChunkStatus.NOISE, chunk);
    }

    private void invokeComposer(ChunkStatus state, Chunk chunk) {
        ChunkPos pos = chunk.getPos();
        this.regionHandler.prepareChunk(pos.getXStart(), pos.getZStart());

        this.compositionProcedure.compose(state, this, chunk, this.regionHandler);
    }

    private void invokeDecorationComposer(ChunkStatus state, class_3233 region) {
        int chunkX = region.method_14336();
        int chunkZ = region.method_14339();
        this.regionHandler.prepareChunk(chunkX << 4, chunkZ << 4);

        this.compositionProcedure.composeDecoration(state, this, region, this.regionHandler);
    }

    @Override
    public void buildSurface(Chunk chunk) {
        this.invokeComposer(ChunkStatus.SURFACE, chunk);
    }

    @Override
    public void carve(Chunk chunk, GenerationStep.Carver carver) {
        if (carver == GenerationStep.Carver.AIR) {
            this.invokeComposer(ChunkStatus.CARVERS, chunk);
        } else {
            this.invokeComposer(ChunkStatus.LIQUID_CARVERS, chunk);
        }
    }

    @Override
    public void generateFeatures(class_3233 region) {
        this.invokeDecorationComposer(ChunkStatus.FEATURES, region);
    }

    @Override
    public void populateEntities(class_3233 region) {
        this.invokeDecorationComposer(ChunkStatus.SPAWN, region);
    }

    @Override
    public int method_12100() {
        return this.world.getSeaLevel() + 1;
    }

    @Override
    public int produceHeight(int x, int z, Heightmap.Type type) {
        this.regionHandler.prepareChunk(x >> 4, z >> 4);
        return this.compositionProcedure.sampleHeight(this.regionHandler, x, z);
    }
}
