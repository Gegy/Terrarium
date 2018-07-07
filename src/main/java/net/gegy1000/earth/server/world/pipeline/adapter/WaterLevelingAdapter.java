package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;

public class WaterLevelingAdapter implements RegionAdapter {
    private static final int LEVEL_RANGE = 8;

    private final RegionComponentType<WaterRasterTile> waterComponent;
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final int oceanLevel;

    public WaterLevelingAdapter(RegionComponentType<WaterRasterTile> waterComponent, RegionComponentType<ShortRasterTile> heightComponent, int oceanLevel) {
        this.waterComponent = waterComponent;
        this.heightComponent = heightComponent;
        this.oceanLevel = oceanLevel;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        WaterRasterTile waterTile = data.getOrExcept(this.waterComponent);
        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int waterType = waterTile.getWaterType(localX, localZ);
                if (waterType == WaterRasterTile.OCEAN) {
                   waterTile.setWaterLevel(localX, localZ, this.oceanLevel);
                } else if (waterType == WaterRasterTile.RIVER) {
                    int levelHeight = Math.max(getLevelHeight(heightTile, localX, localZ, width, height), this.oceanLevel);
                    waterTile.setWaterLevel(localX, localZ, levelHeight);
                }
            }
        }
    }

    private static short getLevelHeight(ShortRasterTile heightTile, int localX, int localZ, int width, int height) {
        short minValue = Short.MAX_VALUE;
        int rangeSquared = LEVEL_RANGE * LEVEL_RANGE;
        for (int offsetZ = -LEVEL_RANGE; offsetZ <= LEVEL_RANGE; offsetZ++) {
            for (int offsetX = -LEVEL_RANGE; offsetX <= LEVEL_RANGE; offsetX++) {
                if (offsetX * offsetX + offsetZ * offsetZ <= rangeSquared) {
                    int globalX = localX + offsetX;
                    int globalZ = localZ + offsetZ;
                    if (globalX >= 0 && globalZ >= 0 && globalX < width && globalZ < height) {
                        short neighbourValue = heightTile.getShort(globalX, globalZ);
                        if (neighbourValue < minValue) {
                            minValue = neighbourValue;
                        }
                    }
                }
            }
        }
        return minValue;
    }
}
