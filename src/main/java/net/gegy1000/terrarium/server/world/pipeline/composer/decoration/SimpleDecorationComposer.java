package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.api.CubicChunkPopulator;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;

public class SimpleDecorationComposer implements DecorationComposer {
    private final CubicChunkPopulator populator;

    protected SimpleDecorationComposer(CubicChunkPopulator populator) {
        this.populator = populator;
    }

    @Override
    public final void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.populator.populate(pos, writer);
    }
}
