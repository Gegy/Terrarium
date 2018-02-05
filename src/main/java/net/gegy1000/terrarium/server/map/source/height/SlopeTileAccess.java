package net.gegy1000.terrarium.server.map.source.height;

import net.gegy1000.terrarium.server.map.source.raster.ByteRasterDataAccess;

public class SlopeTileAccess implements ByteRasterDataAccess {
    private final int width;
    private final int height;
    private final byte[] data;

    public SlopeTileAccess(byte[] data, int width, int height) {
        if (data.length != width * height) {
            throw new IllegalArgumentException("Data length must match given size!");
        }
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    public byte getByte(int x, int y) {
        return this.data[x + y * this.width];
    }

    @Override
    public byte[] getByteData() {
        return this.data;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}
