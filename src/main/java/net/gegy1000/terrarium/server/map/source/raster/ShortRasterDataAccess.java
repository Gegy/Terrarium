package net.gegy1000.terrarium.server.map.source.raster;

import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess;

public interface ShortRasterDataAccess extends TiledDataAccess, RasterDataAccess<Short> {
    @Override
    default Short get(int x, int z) {
        return this.getShort(x, z);
    }

    short getShort(int x, int y);

    @Override
    default Short[] getData() {
        short[] data = this.getShortData();
        Short[] result = new Short[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i];
        }
        return result;
    }

    short[] getShortData();
}
