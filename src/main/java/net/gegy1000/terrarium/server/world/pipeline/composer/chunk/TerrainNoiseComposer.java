package net.gegy1000.terrarium.server.world.pipeline.composer.chunk;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;

public interface TerrainNoiseComposer<C extends TerrariumGeneratorConfig> extends ChunkComposer<C> {
    int sampleHeight(RegionGenerationHandler regionHandler, int x, int z);
}
