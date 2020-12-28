package net.gegy1000.earth.server.world.composer.surface;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.earth.server.world.EarthData;
import net.gegy1000.earth.server.world.geography.Landform;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;

public class WaterFillSurfaceComposer implements SurfaceComposer {
    private final IBlockState block;

    public WaterFillSurfaceComposer(IBlockState block) {
        this.block = block;
    }

    @Override
    public void composeSurface(TerrariumWorld terrarium, ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        data.with(
                EarthData.TERRAIN_HEIGHT,
                EarthData.LANDFORM,
                EarthData.WATER_LEVEL
        ).ifPresent(with -> {
            ShortRaster heightRaster = with.get(EarthData.TERRAIN_HEIGHT);
            EnumRaster<Landform> landformRaster = with.get(EarthData.LANDFORM);
            ShortRaster waterLevelRaster = with.get(EarthData.WATER_LEVEL);

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
        });
    }
}
