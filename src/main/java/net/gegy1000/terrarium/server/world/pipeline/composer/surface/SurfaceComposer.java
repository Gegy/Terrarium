package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.chunk.ChunkPrimer;

public interface SurfaceComposer {
    void composeSurface(ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);
}
