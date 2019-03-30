package net.gegy1000.earth.server.world.pipeline.source.tile;

import net.gegy1000.earth.server.world.cover.SoilClassification;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.RasterData;

import java.util.Arrays;

public class SoilRaster implements RasterData<SoilClassification>, Data {
    private final SoilClassification[] soil;
    private final int width;
    private final int height;

    public SoilRaster(SoilClassification[] soil, int width, int height) {
        if (soil.length != width * height) {
            throw new IllegalArgumentException("Given width and height do not match soil length!");
        }
        this.soil = soil;
        this.width = width;
        this.height = height;
    }

    public SoilRaster(DataView view) {
        this.soil = new SoilClassification[view.getWidth() * view.getHeight()];
        this.width = view.getWidth();
        this.height = view.getHeight();
    }

    public SoilRaster(int width, int height) {
        this(ArrayUtils.defaulted(new SoilClassification[width * height], SoilClassification.NOT_SOIL), width, height);
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
    public void set(int x, int z, SoilClassification value) {
        this.soil[x + z * this.width] = value;
    }

    @Override
    public SoilClassification get(int x, int z) {
        return this.soil[x + z * this.width];
    }

    @Override
    public SoilClassification[] getData() {
        return this.soil;
    }

    @Override
    public Object getRawData() {
        return this.soil;
    }

    @Override
    public SoilRaster copy() {
        return new SoilRaster(Arrays.copyOf(this.soil, this.soil.length), this.width, this.height);
    }
}
