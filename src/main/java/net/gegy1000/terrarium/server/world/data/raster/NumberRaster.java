package net.gegy1000.terrarium.server.world.data.raster;

public interface NumberRaster<T> extends Raster<T> {
    void setDouble(int x, int y, double value);

    double getDouble(int x, int y);
}
