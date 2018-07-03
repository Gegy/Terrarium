package net.gegy1000.terrarium.server.world.pipeline.source.tile;

public interface RasterDataAccess<T> extends TiledDataAccess {
    int getWidth();

    int getHeight();

    void set(int x, int z, T value);

    T get(int x, int z);

    T[] getData();
}
