package net.gegy1000.terrarium.server.world.pipeline.composer.surface;

import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

public class OceanFillSurfaceComposer implements SurfaceComposer {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final IBlockState block;
    private final int oceanLevel;

    public OceanFillSurfaceComposer(RegionComponentType<ShortRasterTile> heightComponent, IBlockState block, int oceanLevel) {
        this.heightComponent = heightComponent;
        this.block = block;
        this.oceanLevel = oceanLevel;
    }

    @Override
    public void composeSurface(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = heightRaster.getShort(localX, localZ);
                if (height < this.oceanLevel) {
                    for (int localY = height + 1; localY <= this.oceanLevel; localY++) {
                        primer.setBlockState(localX, localY, localZ, this.block);
                    }
                }
            }
        }
    }
}
