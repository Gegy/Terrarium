package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRaster;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;

import java.util.Optional;

public class WaterFillSurfaceComposer implements SurfaceComposer {
    private final DataKey<ShortRaster> heightKey;
    private final DataKey<WaterRaster> waterKey;
    private final IBlockState block;

    public WaterFillSurfaceComposer(DataKey<ShortRaster> heightKey, DataKey<WaterRaster> waterKey, IBlockState block) {
        this.heightKey = heightKey;
        this.waterKey = waterKey;
        this.block = block;
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        Optional<ShortRaster> heightOption = data.get(this.heightKey);
        Optional<WaterRaster> waterOption = data.get(this.waterKey);

        if (!heightOption.isPresent() || !waterOption.isPresent()) {
            return;
        }

        ShortRaster heightRaster = heightOption.get();
        WaterRaster waterRaster = waterOption.get();

        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int waterType = waterRaster.getWaterType(localX, localZ);
                if (waterType != WaterRaster.LAND) {
                    int height = Math.max(heightRaster.get(localX, localZ), minY);
                    int waterLevel = Math.min(waterRaster.getWaterLevel(localX, localZ), maxY);
                    if (height < waterLevel) {
                        for (int localY = height + 1; localY <= waterLevel; localY++) {
                            writer.set(localX, localY, localZ, this.block);
                        }
                    }
                }
            }
        }
    }
}
