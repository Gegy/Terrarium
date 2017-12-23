package net.gegy1000.terrarium.server.map.source.raster;

import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess;

public interface RasterDataAccess<T> extends TiledDataAccess {
    int getWidth();

    int getHeight();

    T get(int x, int y);
}
