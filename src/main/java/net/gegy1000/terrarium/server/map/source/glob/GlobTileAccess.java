package net.gegy1000.terrarium.server.map.source.glob;

import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.raster.RasterDataAccess;
import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess;

public class GlobTileAccess implements RasterDataAccess<CoverType>, TiledDataAccess {
    private final byte[] data;
    private final int offsetX;
    private final int offsetZ;
    private final int width;
    private final int height;

    public GlobTileAccess(byte[] data, int offsetX, int offsetZ, int width, int height) {
        this.data = data;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
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
    public CoverType get(int x, int y) {
        byte globId = this.data[(x - this.offsetX) + (y - this.offsetZ) * this.width];
        return CoverType.getGlob(globId & 0xFF);
    }
}
