package dev.gegy.terrarium.backend.raster;

public interface Raster {
    RasterType<? extends Raster> type();

    RasterShape shape();

    default int width() {
        return shape().width();
    }

    default int height() {
        return shape().height();
    }

    @SuppressWarnings("unchecked")
    static <T extends Raster> RasterType<T> type(final T raster) {
        return (RasterType<T>) raster.type();
    }
}
