package net.gegy1000.terrarium.server.map.source.raster;

import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess;

public interface ByteRasterDataAccess extends TiledDataAccess, RasterDataAccess<Byte> {
    @Override
    default Byte get(int x, int z) {
        return this.getByte(x, z);
    }

    byte getByte(int x, int y);

    @Override
    default Byte[] getData() {
        byte[] data = this.getByteData();
        Byte[] result = new Byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i];
        }
        return result;
    }

    byte[] getByteData();
}
