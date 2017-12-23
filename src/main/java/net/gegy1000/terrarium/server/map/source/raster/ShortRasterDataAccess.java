package net.gegy1000.terrarium.server.map.source.raster;

import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess;

public interface ShortRasterDataAccess extends TiledDataAccess {
    int getWidth();

    int getHeight();

    short get(int x, int y);
}
