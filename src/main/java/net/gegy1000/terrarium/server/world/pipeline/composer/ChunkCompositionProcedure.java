package net.gegy1000.terrarium.server.world.pipeline.composer;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.class_3233;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Collection;

public interface ChunkCompositionProcedure<C extends TerrariumGeneratorConfig> {
    void compose(ChunkStatus state, ChunkGenerator<C> generator, Chunk chunk, RegionGenerationHandler regionHandler);

    void composeDecoration(ChunkStatus state, ChunkGenerator<C> generator, class_3233 region, RegionGenerationHandler regionHandler);

    int sampleHeight(RegionGenerationHandler regionHandler, int x, int z);

    Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    Collection<RegionComponentType<?>> getDependencies(ChunkStatus state);

    Collection<RegionComponentType<?>> getBiomeDependencies();
}
