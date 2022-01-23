package net.gegy1000.terrarium.server.world.data.raster;

import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.ColumnDataEntry;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractRaster<T> implements Raster<T> {
    protected final T rawData;
    protected final int width;
    protected final int height;

    protected AbstractRaster(T rawData, int width, int height) {
        this.rawData = rawData;
        this.width = width;
        this.height = height;
    }

    @Override
    public final T asRawData() {
        return this.rawData;
    }

    @Override
    public final int width() {
        return this.width;
    }

    @Override
    public final int height() {
        return this.height;
    }

    protected static <R extends Raster<?>> void sampleInto(R resultRaster, ColumnDataCache dataCache, DataView view, DataKey<R> key) {
        int chunkMinX = view.minX() >> 4;
        int chunkMinY = view.minY() >> 4;
        int chunkMaxX = view.maxX() >> 4;
        int chunkMaxY = view.maxY() >> 4;

        Collection<ColumnDataEntry.Handle> columnHandles = new ArrayList<>();

        for (int chunkY = chunkMinY; chunkY <= chunkMaxY; chunkY++) {
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                columnHandles.add(dataCache.acquireEntry(chunkX, chunkY));
            }
        }

        try {
            for (ColumnDataEntry.Handle handle : columnHandles) {
                ChunkPos columnPos = handle.getColumnPos();

                DataSample data = handle.join();
                data.get(key).ifPresent(sourceRaster -> {
                    DataView srcView = DataView.of(columnPos);
                    Raster.rasterCopy(sourceRaster, srcView, resultRaster, view);
                });
            }
        } finally {
            columnHandles.forEach(ColumnDataEntry.Handle::release);
        }
    }
}
