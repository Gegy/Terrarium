package net.gegy1000.terrarium.server.world.pipeline.composer.biome;

import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.biome.Biome;

public interface BiomeComposer {
    Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ);
}
