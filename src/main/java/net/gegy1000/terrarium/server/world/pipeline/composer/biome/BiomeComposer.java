package net.gegy1000.terrarium.server.world.pipeline.composer.biome;

import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.world.biome.Biome;

public interface BiomeComposer {
    Biome[] composeBiomes(GenerationRegionHandler regionHandler, int chunkX, int chunkZ);
}
