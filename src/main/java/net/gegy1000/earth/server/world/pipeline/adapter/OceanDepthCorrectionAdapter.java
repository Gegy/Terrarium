package net.gegy1000.earth.server.world.pipeline.adapter;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;

public class OceanDepthCorrectionAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final int oceanDepth;

    public OceanDepthCorrectionAdapter(RegionComponentType<ShortRasterTile> heightComponent, int oceanDepth) {
        this.heightComponent = heightComponent;
        this.oceanDepth = oceanDepth;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);

        short[] heightBuffer = heightTile.getShortData();
        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localZ * width;
                if (heightBuffer[index] <= 0) {
                    heightBuffer[index] = (short) -(this.oceanDepth - 1);
                }
            }
        }
    }
}
