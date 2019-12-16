package net.gegy1000.terrarium.server.world.data.raster;

public interface IntegerRaster<T> extends NumberRaster<T> {
    void setInt(int x, int y, int value);

    int getInt(int x, int y);
}
