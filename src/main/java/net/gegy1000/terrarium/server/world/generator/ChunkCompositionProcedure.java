package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

public interface ChunkCompositionProcedure {
    void composeSurface(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    void composeDecoration(IChunkGenerator generator, World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ);
}
