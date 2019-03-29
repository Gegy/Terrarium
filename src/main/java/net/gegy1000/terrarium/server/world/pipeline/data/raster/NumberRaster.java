package net.gegy1000.terrarium.server.world.pipeline.data.raster;

public interface NumberRaster<T extends Number> extends RasterData<T> {
    void setDouble(int x, int y, double value);

    double getDouble(int x, int y);
}
