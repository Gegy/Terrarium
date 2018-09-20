package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.state.IBlockState;

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
    public void composeSurface(RegionGenerationHandler regionHandler, CubicPos pos, ChunkPrimeWriter writer) {
        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);
        WaterRasterTile waterRaster = regionHandler.getCachedChunkRaster(this.waterComponent);

        int minY = pos.getMinY();
        int maxY = pos.getMaxY();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int waterType = waterRaster.getWaterType(localX, localZ);
                if (waterType != WaterRasterTile.LAND) {
                    int height = Math.max(heightRaster.getShort(localX, localZ), minY);
                    int waterLevel = Math.min(waterRaster.getWaterLevel(localX, localZ), maxY);
                    if (height < waterLevel) {
                        for (int localY = height + 1; localY <= waterLevel; localY++) {
                            writer.set(localX, localY, localZ, this.block);
                        }
                    }
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.heightComponent, this.waterComponent };
    }
}
