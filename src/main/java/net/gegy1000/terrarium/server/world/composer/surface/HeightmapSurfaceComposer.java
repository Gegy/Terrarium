package net.gegy1000.terrarium.server.world.composer.surface;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.DataKey;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;

public class HeightmapSurfaceComposer implements SurfaceComposer {
    private final DataKey<ShortRaster> heightKey;
    private final IBlockState block;

    public HeightmapSurfaceComposer(DataKey<ShortRaster> heightKey, IBlockState block) {
        this.heightKey = heightKey;
        this.block = block;
    }

    @Override
    public void composeSurface(TerrariumWorld terrarium, ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
        data.get(this.heightKey).ifPresent(chunkRaster -> {
            int minY = pos.getMinY();
            int maxY = pos.getMaxY();

            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    int height = Math.min(chunkRaster.get(localX, localZ), maxY);
                    for (int y = minY; y <= height; y++) {
                        writer.set(localX, y, localZ, this.block);
                    }
                }
            }
        });
    }
}
