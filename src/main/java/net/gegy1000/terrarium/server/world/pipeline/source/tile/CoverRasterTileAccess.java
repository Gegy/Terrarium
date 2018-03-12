package net.gegy1000.terrarium.server.world.pipeline.source.tile;

import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.cover.CoverType;

public class CoverRasterTileAccess implements RasterDataAccess<CoverType>, TiledDataAccess {
    private final CoverType[] cover;
    private final int offsetX;
    private final int offsetZ;
    private final int width;
    private final int height;

    public CoverRasterTileAccess(CoverType[] cover, int offsetX, int offsetZ, int width, int height) {
        if (cover.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match cover length!");
        }
        this.cover = cover;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.width = width;
        this.height = height;
    }

    public CoverRasterTileAccess(CoverType[] cover, int width, int height) {
        this(cover, 0, 0, width, height);
    }

    public CoverRasterTileAccess(int width, int height) {
        this(ArrayUtils.defaulted(new CoverType[width * height], CoverType.NO_DATA), width, height);
    }

    public static CoverRasterTileAccess loadGlob(byte[] data, int offsetX, int offsetZ, int width, int height) {
        CoverType[] cover = new CoverType[data.length];
        for (int i = 0; i < data.length; i++) {
            cover[i] = CoverType.getGlob(data[i] & 0xFF);
        }
        return new CoverRasterTileAccess(cover, offsetX, offsetZ, width, height);
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
    public void set(int x, int z, CoverType value) {
        this.cover[(x - this.offsetX) + (z - this.offsetZ) * this.width] = value;
    }

    @Override
    public CoverType get(int x, int z) {
        return this.cover[(x - this.offsetX) + (z - this.offsetZ) * this.width];
    }

    @Override
    public CoverType[] getData() {
        return this.cover;
    }
}
