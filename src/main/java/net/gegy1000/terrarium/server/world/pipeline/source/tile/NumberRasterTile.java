package net.gegy1000.terrarium.server.world.pipeline.source.tile;

public interface NumberRasterTile<T extends Number> extends RasterDataAccess<T> {
    void setDouble(int x, int y, double value);

    double getDouble(int x, int y);
}
