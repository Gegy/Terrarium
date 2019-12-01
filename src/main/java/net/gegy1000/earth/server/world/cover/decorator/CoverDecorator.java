package net.gegy1000.earth.server.world.cover.decorator;

import net.gegy1000.gengen.api.ChunkPopulationWriter;
import net.gegy1000.gengen.api.CubicPos;

import java.util.Random;

public interface CoverDecorator {
    void decorate(ChunkPopulationWriter writer, CubicPos pos, Random random);
}
