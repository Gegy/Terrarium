package net.gegy1000.terrarium.server.map.source.height;

import net.gegy1000.terrarium.server.map.source.raster.ShortRasterDataAccess;

public class HeightTileAccess implements ShortRasterDataAccess {
    private final int width;
    private final int height;
    private final short[] data;

    public HeightTileAccess(short[] data, int width, int height) {
        if (data.length != width * height) {
            throw new IllegalArgumentException("Data length must match given size!");
        }
        this.data = data;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public short getShort(int x, int y) {
        return this.data[x + y * this.width];
    }

    @Override
    public short[] getShortData() {
        return this.data;
    }
}
