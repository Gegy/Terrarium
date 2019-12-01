package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.gengen.api.ChunkPopulationWriter;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.GenericChunkPopulator;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;

public class SimpleDecorationComposer implements DecorationComposer {
    private final GenericChunkPopulator populator;

    protected SimpleDecorationComposer(GenericChunkPopulator populator) {
        this.populator = populator;
    }

    @Override
    public final void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
        this.populator.populate(pos, writer);
    }
}
