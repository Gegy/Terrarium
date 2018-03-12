package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.world.chunk.ChunkPrimer;

public interface SurfaceComposer {
    void provideSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ);
}
