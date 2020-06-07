package net.gegy1000.terrarium.server.world.composer.surface;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.ColumnData;

public interface SurfaceComposer {
    void composeSurface(TerrariumWorld terrarium, ColumnData data, CubicPos pos, ChunkPrimeWriter writer);
}
