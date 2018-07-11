package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public interface ChunkCompositionProcedure {
    void composeSurface(ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    void composeDecoration(World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);

    Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ);
}
