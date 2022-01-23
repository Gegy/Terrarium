package net.gegy1000.terrarium.server.world.data.raster;

import com.google.common.base.Functions;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public interface NumberRaster<T> extends Raster<T> {
    void setFloat(int x, int y, float value);

    float getFloat(int x, int y);

    default DoubleStream stream() {
        return IntStream.range(0, this.height())
                .mapToObj(y -> IntStream.range(0, this.width())
                        .mapToDouble(x -> this.getFloat(x, y))
                )
                .flatMapToDouble(Functions.identity());
    }
}
