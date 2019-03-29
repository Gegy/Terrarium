package net.gegy1000.earth.server.world.pipeline.source.tile;

import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;

public class WaterRaster extends ShortRaster {
    public static final int WATER_TYPE_MASK = 0x3;
    public static final int WATER_LEVEL_MASK = 0x3FFC;

    public static final int LAND = 0;
    public static final int OCEAN = 1;
    public static final int RIVER = 2;
    public static final int RIVER_CENTER = 3;

    public WaterRaster(short[] data, int width, int height) {
        super(data, width, height);
    }

    public WaterRaster(DataView view) {
        super(view);
    }

    public void setWaterType(int localX, int localZ, int type) {
        this.setShort(localX, localZ, (short) (type & WATER_TYPE_MASK));
    }

    public void setWaterLevel(int localX, int localZ, int level) {
        int waterType = this.getShort(localX, localZ) & WATER_TYPE_MASK;
        this.setShort(localX, localZ, (short) (waterType | level << 2 & WATER_LEVEL_MASK));
    }

    public int getWaterType(int localX, int localZ) {
        return this.getShort(localX, localZ) & WATER_TYPE_MASK;
    }

    public int getWaterLevel(int localX, int localZ) {
        return (this.getShort(localX, localZ) & WATER_LEVEL_MASK) >> 2;
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
