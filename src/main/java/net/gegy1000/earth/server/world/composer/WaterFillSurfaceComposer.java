package net.gegy1000.earth.server.world.composer;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.DataKey;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;

import java.util.Optional;

public class WaterFillSurfaceComposer implements SurfaceComposer {
    private final DataKey<ShortRaster> heightKey;
    private final DataKey<EnumRaster<Landform>> landformKey;
    private final DataKey<ShortRaster> waterLevelKey;
    private final IBlockState block;

    public WaterFillSurfaceComposer(DataKey<ShortRaster> heightKey, DataKey<EnumRaster<Landform>> landformKey, DataKey<ShortRaster> waterLevelKey, IBlockState block) {
        this.heightKey = heightKey;
        this.landformKey = landformKey;
        this.waterLevelKey = waterLevelKey;
        this.block = block;
    }

    @Override
    public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        Optional<ShortRaster> heightOption = data.get(this.heightKey);
        Optional<EnumRaster<Landform>> landformOption = data.get(this.landformKey);
        Optional<ShortRaster> waterLevelOption = data.get(this.waterLevelKey);

        if (!heightOption.isPresent() || !waterLevelOption.isPresent() || !landformOption.isPresent()) {
            return;
        }

        ShortRaster heightRaster = heightOption.get();
        EnumRaster<Landform> landformRaster = landformOption.get();
        ShortRaster waterLevelRaster = waterLevelOption.get();

        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

        landformRaster.iterate((landform, localX, localZ) -> {
            if (landform.isWater()) {
                int height = Math.max(heightRaster.get(localX, localZ), minY);
                int waterLevel = Math.min(waterLevelRaster.get(localX, localZ), maxY);
                if (height < waterLevel) {
                    for (int y = height + 1; y <= waterLevel; y++) {
                        writer.set(localX, y, localZ, this.block);
                    }
                }
            }
        });
    }
}
