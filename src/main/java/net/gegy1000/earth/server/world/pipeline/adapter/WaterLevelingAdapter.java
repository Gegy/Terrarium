package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.util.SpiralIterator;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;

import javax.annotation.Nullable;
import javax.vecmath.Point2i;

public class WaterLevelingAdapter implements RegionAdapter {
    private static final int CENTER_RANGE = GenerationRegion.BUFFER;
    private static final float CENTER_RANGE_SQUARED = (CENTER_RANGE * CENTER_RANGE) * 2.0F;

    private final RegionComponentType<WaterRaster> waterComponent;
    private final RegionComponentType<ShortRaster> heightComponent;
    private final int oceanLevel;

    public WaterLevelingAdapter(RegionComponentType<WaterRaster> waterComponent, RegionComponentType<ShortRaster> heightComponent, int oceanLevel) {
        this.waterComponent = waterComponent;
        this.heightComponent = heightComponent;
        this.oceanLevel = oceanLevel;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        WaterRaster waterTile = data.getOrExcept(this.waterComponent);
        ShortRaster heightTile = data.getOrExcept(this.heightComponent);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int waterType = waterTile.getWaterType(localX, localZ);
                if (waterType == WaterRaster.OCEAN) {
                    waterTile.setWaterLevel(localX, localZ, this.oceanLevel);
                } else if (waterType == WaterRaster.RIVER_CENTER) {
                    int levelHeight = Math.max(heightTile.getShort(localX, localZ), this.oceanLevel);
                    waterTile.setWaterLevel(localX, localZ, levelHeight);
                } else if (waterType == WaterRaster.RIVER) {
                    int riverLevel = this.getRiverLevel(waterTile, heightTile, localX, localZ, width, height);
                    waterTile.setWaterLevel(localX, localZ, riverLevel);
                }
            }
        }
    }

    private int getRiverLevel(WaterRaster waterTile, ShortRaster heightTile, int localX, int localZ, int width, int height) {
        Point2i riverCenter = getRiverCenter(waterTile, localX, localZ, width, height);
        if (riverCenter != null) {
            int levelHeight = Math.max(heightTile.getShort(localX, localZ), this.oceanLevel);
            int centerHeight = Math.max(heightTile.getShort(riverCenter.x, riverCenter.y), this.oceanLevel);
            if (centerHeight == levelHeight) {
                return centerHeight;
            }
            int deltaX = riverCenter.x - localX;
            int deltaZ = riverCenter.y - localZ;
            int distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
            float alpha = distanceSquared / CENTER_RANGE_SQUARED;
            return (int) (centerHeight + (levelHeight - centerHeight) * alpha);
        }
        return Math.max(getLevelHeight(heightTile, localX, localZ, width, height), this.oceanLevel);
    }

    private static short getLevelHeight(ShortRaster heightTile, int localX, int localZ, int width, int height) {
        int totalValue = 0;
        int count = 0;
        for (int offsetZ = -CENTER_RANGE; offsetZ <= CENTER_RANGE; offsetZ++) {
            for (int offsetX = -CENTER_RANGE; offsetX <= CENTER_RANGE; offsetX++) {
                int globalX = localX + offsetX;
                int globalZ = localZ + offsetZ;
                if (globalX >= 0 && globalZ >= 0 && globalX < width && globalZ < height) {
                    totalValue += heightTile.getShort(globalX, globalZ);
                    count++;
                }
            }
        }
        return (short) (totalValue / count);
    }

    @Nullable
    private static Point2i getRiverCenter(WaterRaster waterTile, int localX, int localZ, int width, int height) {
        for (Point2i point : SpiralIterator.of(GenerationRegion.BUFFER)) {
            int globalX = point.x + localX;
            int globalY = point.y + localZ;
            if (globalX >= 0 && globalY >= 0 && globalX < width && globalY < height) {
                int waterType = waterTile.getWaterType(globalX, globalY);
                if (waterType == WaterRaster.RIVER_CENTER) {
                    return new Point2i(globalX, globalY);
                }
            }
        }
        return null;
    }
}
