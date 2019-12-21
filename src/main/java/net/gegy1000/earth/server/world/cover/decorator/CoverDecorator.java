package net.gegy1000.earth.server.world.cover.decorator;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;

import java.util.Random;

public interface CoverDecorator {
    void decorate(ColumnDataCache dataCache, ChunkPopulationWriter writer, CubicPos pos, Random random);
}
