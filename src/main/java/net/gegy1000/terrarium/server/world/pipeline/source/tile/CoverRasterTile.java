package net.gegy1000.terrarium.server.world.pipeline.source.tile;

import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.pipeline.DataView;

import java.util.Arrays;

public class CoverRasterTile implements RasterDataAccess<CoverType>, TiledDataAccess {
    private final CoverType[] cover;
    private final int offsetX;
    private final int offsetZ;
    private final int width;
    private final int height;

    public CoverRasterTile(CoverType[] cover, int offsetX, int offsetZ, int width, int height) {
        if (cover.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match cover length!");
        }
        this.cover = cover;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.width = width;
        this.height = height;
    }

    public CoverRasterTile(DataView view) {
        this.cover = new CoverType[view.getWidth() * view.getHeight()];
        this.offsetX = 0;
        this.offsetZ = 0;
        this.width = view.getWidth();
        this.height = view.getHeight();
    }

    public CoverRasterTile(CoverType[] cover, int width, int height) {
        this(cover, 0, 0, width, height);
    }

    public CoverRasterTile(int width, int height) {
        this(ArrayUtils.defaulted(new CoverType[width * height], TerrariumCoverTypes.PLACEHOLDER), width, height);
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

    @Override
    public CoverRasterTile copy() {
        return new CoverRasterTile(Arrays.copyOf(this.cover, this.cover.length), this.width, this.height);
    }
}
