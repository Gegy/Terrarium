package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;

public class WaterCarveAdapter implements RegionAdapter {
    private final RegionComponentType<WaterRasterTile> waterComponent;
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final int oceanDepth;

    public WaterCarveAdapter(RegionComponentType<WaterRasterTile> waterComponent, RegionComponentType<ShortRasterTile> heightComponent, int oceanDepth) {
        this.waterComponent = waterComponent;
        this.heightComponent = heightComponent;
        this.oceanDepth = oceanDepth;
    }

    @Override
    public void adapt(RegionData data, int x, int z, int width, int height) {
        WaterRasterTile waterTile = data.getOrExcept(this.waterComponent);
        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int waterType = waterTile.getWaterType(localX, localZ);
                if (waterType != WaterRasterTile.LAND) {
                    // TODO: Handle smooth edges
                    int waterLevel = waterTile.getWaterLevel(localX, localZ);
                    int depth = waterType == WaterRasterTile.OCEAN ? this.oceanDepth : Math.min(this.oceanDepth, 4);
                    heightTile.setShort(localX, localZ, (short) (waterLevel - depth));
                }
            }
        }
    }
}
