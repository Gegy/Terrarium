package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.gegy1000.terrarium.server.world.chunk.prime.PrimeChunk;
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
    public void composeSurface(RegionGenerationHandler regionHandler, PrimeChunk chunk) {
        ShortRasterTile chunkRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        CubicPos pos = chunk.getPos();
        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

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
