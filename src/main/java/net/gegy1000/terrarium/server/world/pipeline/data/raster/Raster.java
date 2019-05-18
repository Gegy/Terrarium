package net.gegy1000.terrarium.server.world.pipeline.data.raster;

import net.gegy1000.terrarium.server.world.pipeline.data.Data;

public interface Raster<T> extends Data {
    int getWidth();

    int getHeight();

    T getData();

    default int index(int x, int y) {
        return x + y * this.getWidth();
    }

    @Override
    Raster<T> copy();
}
