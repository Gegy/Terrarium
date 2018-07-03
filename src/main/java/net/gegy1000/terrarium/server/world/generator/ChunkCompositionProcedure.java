package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;

public interface ChunkCompositionProcedure {
    void composeSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ);

    void composeDecoration(World world, GenerationRegionHandler regionHandler, int chunkX, int chunkZ);

    Biome[] composeBiomes(GenerationRegionHandler regionHandler, int chunkX, int chunkZ);
}
