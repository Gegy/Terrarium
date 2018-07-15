package net.gegy1000.terrarium.server.world.pipeline.composer.biome;

import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.biome.Biome;

public interface BiomeComposer extends ChunkComposer {
    Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ);
}
