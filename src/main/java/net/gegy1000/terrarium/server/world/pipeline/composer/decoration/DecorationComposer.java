package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.gengen.api.ChunkPopulationWriter;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;

public interface DecorationComposer {
    void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer);
}
