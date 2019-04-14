package net.gegy1000.earth.server.world.pipeline.source.tile;

import net.gegy1000.earth.server.world.cover.CoverClassification;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.RasterData;

import java.util.Arrays;

public class CoverRaster implements RasterData<CoverClassification>, Data {
    private final CoverClassification[] cover;
    private final int width;
    private final int height;

    public CoverRaster(CoverClassification[] cover, int width, int height) {
        if (cover.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match cover length!");
        }
        this.cover = cover;
        this.width = width;
        this.height = height;
    }

    public CoverRaster(DataView view) {
        this.cover = new CoverClassification[view.getWidth() * view.getHeight()];
        this.width = view.getWidth();
        this.height = view.getHeight();
    }

    public CoverRaster(int width, int height) {
        this(ArrayUtils.defaulted(new CoverClassification[width * height], CoverClassification.NO_DATA), width, height);
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
    public void set(int x, int z, CoverClassification value) {
        this.cover[x + z * this.width] = value;
    }

    @Override
    public CoverClassification get(int x, int z) {
        return this.cover[x + z * this.width];
    }

    @Override
    public CoverClassification[] getData() {
        return this.cover;
    }

    @Override
    public Object getRawData() {
        return this.cover;
    }

    @Override
    public CoverRaster copy() {
        return new CoverRaster(Arrays.copyOf(this.cover, this.cover.length), this.width, this.height);
    }
}
