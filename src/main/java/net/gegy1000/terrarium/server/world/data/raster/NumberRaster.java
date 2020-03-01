package net.gegy1000.terrarium.server.world.data.raster;

import com.google.common.base.Functions;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public interface NumberRaster<T> extends Raster<T> {
    void setDouble(int x, int y, double value);

    double getDouble(int x, int y);

    default DoubleStream stream() {
        return IntStream.range(0, this.getHeight())
                .mapToObj(y -> IntStream.range(0, this.getWidth())
                        .mapToDouble(x -> this.getDouble(x, y))
                )
                .flatMapToDouble(Functions.identity());
    }
}
