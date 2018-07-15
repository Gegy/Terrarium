package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

public interface SurfaceComposer extends ChunkComposer {
    void composeSurface(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ);
}
