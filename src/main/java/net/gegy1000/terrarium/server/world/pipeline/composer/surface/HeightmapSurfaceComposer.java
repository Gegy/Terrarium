package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

public class HeightmapSurfaceComposer implements SurfaceComposer {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final IBlockState block;

    public HeightmapSurfaceComposer(RegionComponentType<ShortRasterTile> heightComponent, IBlockState block) {
        this.heightComponent = heightComponent;
        this.block = block;
    }

    @Override
    public void composeSurface(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        ShortRasterTile chunkRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = chunkRaster.getShort(localX, localZ);
                for (int localY = 1; localY <= height; localY++) {
                    primer.setBlockState(localX, localY, localZ, this.block);
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.heightComponent };
    }
}
