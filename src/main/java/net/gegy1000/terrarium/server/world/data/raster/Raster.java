package net.gegy1000.terrarium.server.world.data.raster;

import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataView;

import java.util.function.BiFunction;

public interface Raster<T> {
    int getWidth();

    int getHeight();

    T getData();

    default int index(int x, int y) {
        return x + y * this.getWidth();
    }

    default DataView asView() {
        return DataView.rect(this.getWidth(), this.getHeight());
    }

    @SuppressWarnings({ "SuspiciousSystemArraycopy" })
    static <R extends Raster<?>> void rasterCopy(R src, DataView srcView, R dest, DataView destView) {
        int minX = Math.max(0, destView.getMinX() - srcView.getMinX());
        int minY = Math.max(0, destView.getMinY() - srcView.getMinY());
        int maxX = Math.min(srcView.getWidth(), destView.getMaxX() - srcView.getMinX());
        int maxY = Math.min(srcView.getHeight(), destView.getMaxY() - srcView.getMinY());

        Object srcData = src.getData();
        Object destData = dest.getData();

        for (int localY = minY; localY < maxY; localY++) {
            int resultY = (localY + srcView.getMinY()) - destView.getMinY();

            int localX = minX;
            int resultX = (localX + srcView.getMinX()) - destView.getMinX();

            int sourceIndex = localX + localY * src.getWidth();
            int resultIndex = resultX + resultY * dest.getWidth();

            System.arraycopy(srcData, sourceIndex, destData, resultIndex, maxX - minX);
        }
    }

    @SuppressWarnings({ "SuspiciousSystemArraycopy" })
    static <R extends Raster<?>> void rasterCopy(R src, R dest) {
        int length = src.getWidth() * src.getHeight();
        System.arraycopy(src.getData(), 0, dest.getData(), 0, length);
    }

    interface Sampler<T> extends BiFunction<ColumnDataCache, DataView, T> {
        T sample(ColumnDataCache dataCache, DataView view);

        @Override
        default T apply(ColumnDataCache dataCache, DataView view) {
            return this.sample(dataCache, view);
        }
    }
}
