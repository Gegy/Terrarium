package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

public class WaterFillSurfaceComposer implements SurfaceComposer {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<WaterRasterTile> waterComponent;
    private final IBlockState block;

    public WaterFillSurfaceComposer(RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<WaterRasterTile> waterComponent, IBlockState block) {
        this.heightComponent = heightComponent;
        this.waterComponent = waterComponent;
        this.block = block;
    }

    @Override
    public void composeSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);
        WaterRasterTile waterRaster = regionHandler.getCachedChunkRaster(this.waterComponent);

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int waterType = waterRaster.getWaterType(localX, localZ);
                if (waterType != WaterRasterTile.LAND) {
                    int height = heightRaster.getShort(localX, localZ);
                    int waterLevel = waterRaster.getWaterLevel(localX, localZ);
                    if (height < waterLevel) {
                        for (int localY = height + 1; localY <= waterLevel; localY++) {
                            primer.setBlockState(localX, localY, localZ, this.block);
                        }
                    }
                }
            }
        }
    }
}
