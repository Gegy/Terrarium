package net.gegy1000.earth.server.world.cover.decorator;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;

import java.util.Random;

public interface CoverDecorator {
    void decorate(ChunkPopulationWriter writer, CubicPos pos, Random random);
}
