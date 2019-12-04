package net.gegy1000.terrarium.server.world.data.raster;

import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.ColumnDataEntry;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractRaster<T> implements Raster<T> {
    protected final T data;
    protected final int width;
    protected final int height;

    protected AbstractRaster(T data, int width, int height) {
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    public final T getData() {
        return this.data;
    }

    @Override
    public final int getWidth() {
        return this.width;
    }

    @Override
    public final int getHeight() {
        return this.height;
    }

    @SuppressWarnings({ "SuspiciousSystemArraycopy" })
    protected static <T, R extends AbstractRaster<T>> void sampleInto(R resultRaster, ColumnDataCache dataCache, DataView view, DataKey<R> key) {
        int chunkMinX = view.getX() >> 4;
        int chunkMinY = view.getY() >> 4;
        int chunkMaxX = view.getMaxX() >> 4;
        int chunkMaxY = view.getMaxY() >> 4;

        Collection<ColumnDataEntry.Handle> columnHandles = new ArrayList<>();

        for (int chunkY = chunkMinY; chunkY <= chunkMaxY; chunkY++) {
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                ChunkPos columnPos = new ChunkPos(chunkX, chunkY);
                columnHandles.add(dataCache.acquireEntry(columnPos));
            }
        }

        try {
            for (ColumnDataEntry.Handle handle : columnHandles) {
                ChunkPos columnPos = handle.getColumnPos();

                ColumnData data = handle.join();
                data.get(key).ifPresent(sourceRaster -> {
                    int minColumnX = columnPos.getXStart();
                    int minColumnY = columnPos.getZStart();

                    int minX = Math.max(0, view.getMinX() - minColumnX);
                    int minY = Math.max(0, view.getMinY() - minColumnY);
                    int maxX = Math.min(sourceRaster.getWidth(), view.getMaxX() - minColumnX);
                    int maxY = Math.min(sourceRaster.getHeight(), view.getMaxY() - minColumnY);

                    for (int localY = minY; localY < maxY; localY++) {
                        int resultY = (localY + minColumnY) - view.getMinY();

                        int localX = minX;
                        int resultX = (localX + minColumnX) - view.getMinX();

                        int sourceIndex = localX + localY * sourceRaster.getWidth();
                        int resultIndex = resultX + resultY * resultRaster.getWidth();

                        System.arraycopy(sourceRaster.data, sourceIndex, resultRaster.data, resultIndex, maxX - minX);
                    }
                });
            }
        } finally {
            columnHandles.forEach(ColumnDataEntry.Handle::release);
        }
    }
}
