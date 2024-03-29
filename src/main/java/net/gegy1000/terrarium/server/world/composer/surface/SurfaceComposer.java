package net.gegy1000.terrarium.server.world.composer.surface;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.DataSample;

public interface SurfaceComposer {
    void composeSurface(TerrariumWorld terrarium, DataSample data, CubicPos pos, ChunkPrimeWriter writer);
}
