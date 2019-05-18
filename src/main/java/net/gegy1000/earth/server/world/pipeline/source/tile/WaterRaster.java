package net.gegy1000.earth.server.world.pipeline.source.tile;

import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.AbstractRaster;

import java.util.Arrays;

public final class WaterRaster extends AbstractRaster<short[]> {
    public static final int WATER_TYPE_MASK = 0x3;
    public static final int WATER_LEVEL_MASK = 0x3FFC;

    public static final int LAND = 0;
    public static final int OCEAN = 1;
    public static final int RIVER = 2;
    public static final int RIVER_CENTER = 3;

    private WaterRaster(short[] data, int width, int height) {
        super(data, width, height);
    }

    public static WaterRaster create(int width, int height) {
        short[] array = new short[width * height];
        return new WaterRaster(array, width, height);
    }

    public static WaterRaster create(DataView view) {
        return create(view.getWidth(), view.getHeight());
    }

    public void setWaterType(int x, int y, int type) {
        this.set(x, y, (short) (type & WATER_TYPE_MASK));
    }

    public void setWaterLevel(int x, int y, int level) {
        int waterType = this.get(x, y) & WATER_TYPE_MASK;
        this.set(x, y, (short) (waterType | level << 2 & WATER_LEVEL_MASK));
    }

    public int getWaterType(int x, int y) {
        return this.get(x, y) & WATER_TYPE_MASK;
    }

    public int getWaterLevel(int x, int y) {
        return (this.get(x, y) & WATER_LEVEL_MASK) >> 2;
    }

    private void set(int x, int y, short value) {
        this.data[this.index(x, y)] = value;
    }

    private short get(int x, int y) {
        return this.data[this.index(x, y)];
    }

    @Override
    public WaterRaster copy() {
        return new WaterRaster(Arrays.copyOf(this.data, this.data.length), this.width, this.height);
    }

    public static boolean isLand(int value) {
        int type = value & WATER_TYPE_MASK;
        return type == LAND;
    }

    public static boolean isWater(int value) {
        int type = value & WATER_TYPE_MASK;
        return type == OCEAN || type == RIVER || type == RIVER_CENTER;
    }
}
