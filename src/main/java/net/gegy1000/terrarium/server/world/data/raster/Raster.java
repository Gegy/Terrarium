package net.gegy1000.terrarium.server.world.data.raster;

import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.DataView;

import java.util.function.BiFunction;

public interface Raster<T> {
    int width();

    int height();

    T asRawData();

    default int index(int x, int y) {
        return x + y * this.width();
    }

    default DataView asView() {
        return DataView.of(this.width(), this.height());
    }

    @SuppressWarnings({ "SuspiciousSystemArraycopy" })
    static <R extends Raster<?>> void rasterCopy(R src, DataView srcView, R dst, DataView dstView) {
        int minX = Math.max(0, dstView.minX() - srcView.minX());
        int minY = Math.max(0, dstView.minY() - srcView.minY());
        int maxX = Math.min(srcView.width(), dstView.maxX() - srcView.minX() + 1);
        int maxY = Math.min(srcView.height(), dstView.maxY() - srcView.minY() + 1);

        Object srcRaw = src.asRawData();
        Object dstRaw = dst.asRawData();

        for (int localY = minY; localY < maxY; localY++) {
            int resultY = (localY + srcView.minY()) - dstView.minY();

            int localX = minX;
            int resultX = (localX + srcView.minX()) - dstView.minX();

            int sourceIndex = localX + localY * src.width();
            int resultIndex = resultX + resultY * dst.width();

            System.arraycopy(srcRaw, sourceIndex, dstRaw, resultIndex, maxX - minX);
        }
    }

    @SuppressWarnings({ "SuspiciousSystemArraycopy" })
    static <R extends Raster<?>> void rasterCopy(R src, R dest) {
        int length = src.width() * src.height();
        System.arraycopy(src.asRawData(), 0, dest.asRawData(), 0, length);
    }

    interface Sampler<T> extends BiFunction<ColumnDataCache, DataView, T> {
        T sample(ColumnDataCache dataCache, DataView view);

        @Override
        default T apply(ColumnDataCache dataCache, DataView view) {
            return this.sample(dataCache, view);
        }
    }
}
