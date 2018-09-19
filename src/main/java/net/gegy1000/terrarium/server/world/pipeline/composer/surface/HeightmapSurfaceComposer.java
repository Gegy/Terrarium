package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.chunk.ComposeChunk;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.state.IBlockState;

public class HeightmapSurfaceComposer implements SurfaceComposer {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final IBlockState block;

    public HeightmapSurfaceComposer(RegionComponentType<ShortRasterTile> heightComponent, IBlockState block) {
        this.heightComponent = heightComponent;
        this.block = block;
    }

    @Override
    public void composeSurface(ComposeChunk chunk, RegionGenerationHandler regionHandler) {
        ShortRasterTile chunkRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        int minY = chunk.getMinY();
        int maxY = chunk.getMaxY();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = Math.min(chunkRaster.getShort(localX, localZ), maxY);
                for (int localY = minY; localY <= height; localY++) {
                    chunk.set(localX, localY, localZ, this.block);
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.heightComponent };
    }
}
