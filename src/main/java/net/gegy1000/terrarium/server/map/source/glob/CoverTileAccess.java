package net.gegy1000.terrarium.server.map.source.glob;

import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.raster.RasterDataAccess;
import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess;

public class CoverTileAccess implements RasterDataAccess<CoverType>, TiledDataAccess {
    private final CoverType[] cover;
    private final int offsetX;
    private final int offsetZ;
    private final int width;
    private final int height;

    public CoverTileAccess(CoverType[] cover, int offsetX, int offsetZ, int width, int height) {
        if (cover.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match cover length!");
        }
        this.cover = cover;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.width = width;
        this.height = height;
    }

    public CoverTileAccess(CoverType[] cover, int width, int height) {
        this(cover, 0, 0, width, height);
    }

    public static CoverTileAccess loadGlob(byte[] data, int offsetX, int offsetZ, int width, int height) {
        CoverType[] cover = new CoverType[data.length];
        for (int i = 0; i < data.length; i++) {
            cover[i] = CoverType.getGlob(data[i] & 0xFF);
        }
        return new CoverTileAccess(cover, offsetX, offsetZ, width, height);
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
    public CoverType get(int x, int y) {
        return this.cover[(x - this.offsetX) + (y - this.offsetZ) * this.width];
    }

    @Override
    public CoverType[] getData() {
        return this.cover;
    }
}
