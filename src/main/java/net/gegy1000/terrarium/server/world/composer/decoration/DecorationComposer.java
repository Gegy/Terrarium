package net.gegy1000.terrarium.server.world.composer.decoration;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;

public interface DecorationComposer {
    void composeDecoration(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer);
}
