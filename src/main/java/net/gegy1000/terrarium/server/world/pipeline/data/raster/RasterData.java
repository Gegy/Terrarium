package net.gegy1000.terrarium.server.world.pipeline.data.raster;

import net.gegy1000.terrarium.server.world.pipeline.data.Data;

public interface RasterData<T> extends Data {
    int getWidth();

    int getHeight();

    void set(int x, int z, T value);

    T get(int x, int z);

    T[] getData();

    Object getRawData();
}
