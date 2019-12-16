package net.gegy1000.earth.server.world.composer;

import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
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

        landformRaster.iterate((landform, x, z) -> {
            if (!landform.isWater()) return;

            int height = heightRaster.get(x, z);
            int waterLevel = waterLevelRaster.get(x, z);
            if (height >= waterLevel) return;

            int startY = Math.max(height + 1, minY);
            int endY = Math.min(waterLevel, maxY);
            for (int y = startY; y <= endY; y++) {
                writer.set(x, y, z, this.block);
            }
        });
    }
}
