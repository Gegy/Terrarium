package net.gegy1000.terrarium.server.world.pipeline.adapter;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.MathHelper;

public class HeightTransformAdapter implements RegionAdapter {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final double heightScale;
    private final int heightOffset;

    public HeightTransformAdapter(RegionComponentType<ShortRasterTile> heightComponent, double heightScale, int heightOffset) {
        this.heightComponent = heightComponent;
        this.heightScale = heightScale;
        this.heightOffset = heightOffset;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);
        short[] heightBuffer = heightTile.getShortData();

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localZ * width;
                int scaledHeight = MathHelper.ceil(heightBuffer[index] * this.heightScale);
                heightBuffer[index] = (short) MathHelper.clamp(scaledHeight + this.heightOffset, 1, 255);
            }
        }
    }
}
