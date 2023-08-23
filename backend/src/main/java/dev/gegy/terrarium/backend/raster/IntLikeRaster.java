package dev.gegy.terrarium.backend.raster;

public interface IntLikeRaster extends Raster {
    @Override
    RasterType<? extends IntLikeRaster> type();

    void putInt(int x, int y, int value);

    int getInt(int x, int y);
}
