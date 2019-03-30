package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;

public interface SurfaceComposer extends ChunkComposer {
    void composeSurface(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPrimeWriter writer);
}
