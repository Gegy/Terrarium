package net.gegy1000.earth.server.world.cover.carver;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

// Totally didn't get this idea from 1.13
public interface CoverCarver {
    static int[] sampleChunk(CubicPos pos, GenLayer layer) {
        IntCache.resetIntCache();
        return layer.getInts(pos.getMinX(), pos.getMinZ(), 16, 16);
    }

    void carve(CubicPos pos, ChunkPrimeWriter writer, ColumnData data);
}
