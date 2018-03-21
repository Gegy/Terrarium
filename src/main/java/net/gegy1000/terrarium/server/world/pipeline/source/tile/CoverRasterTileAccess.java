package net.gegy1000.terrarium.server.world.pipeline.source.tile;

import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.CoverTypeRegistry;

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
        this(ArrayUtils.defaulted(new CoverType[width * height], CoverTypeRegistry.PLACEHOLDER), width, height);
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
