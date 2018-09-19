package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.chunk.prime.PrimeChunk;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;

public interface SurfaceComposer extends ChunkComposer {
    void composeSurface(RegionGenerationHandler regionHandler, PrimeChunk chunk);
}
