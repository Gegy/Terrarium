package dev.gegy.terrarium.backend.raster;

import java.util.function.Function;

public interface RasterType<T extends Raster> {
    static <T extends Raster> RasterType<T> create(final Function<RasterShape, T> factory) {
        return new RasterType<>() {
            @Override
            public T create(final RasterShape shape) {
                return factory.apply(shape);
            }
        };
    }

    T create(RasterShape shape);
}
