package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;

public interface DecorationComposer extends ChunkComposer {
    void composeDecoration(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPopulationWriter writer);
}
