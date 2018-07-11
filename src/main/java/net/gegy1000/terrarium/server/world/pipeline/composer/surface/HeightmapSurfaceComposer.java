package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

public class HeightmapSurfaceComposer implements SurfaceComposer {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final IBlockState block;

    private final ShortRasterTile chunkBuffer = new ShortRasterTile(new short[16 * 16], 16, 16);

    public HeightmapSurfaceComposer(RegionComponentType<ShortRasterTile> heightComponent, IBlockState block) {
        this.heightComponent = heightComponent;
        this.block = block;
    }

    @Override
    public void composeSurface(ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        regionHandler.fillRaster(this.heightComponent, this.chunkBuffer, globalX, globalZ, 16, 16);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = this.chunkBuffer.getShort(localX, localZ);
                for (int localY = 1; localY <= height; localY++) {
                    primer.setBlockState(localX, localY, localZ, this.block);
                }
            }
        }
    }
}
