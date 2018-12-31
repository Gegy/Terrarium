package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.ChunkComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.BlockState;
import net.minecraft.class_2919;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class WaterFillComposer<C extends TerrariumGeneratorConfig> implements ChunkComposer<C> {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<WaterRasterTile> waterComponent;
    private final BlockState block;

    public WaterFillComposer(RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<WaterRasterTile> waterComponent, BlockState block) {
        this.heightComponent = heightComponent;
        this.waterComponent = waterComponent;
        this.block = block;
    }

    @Override
    public void compose(ChunkGenerator<C> generator, Chunk chunk, class_2919 random, RegionGenerationHandler regionHandler) {
        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);
        WaterRasterTile waterRaster = regionHandler.getCachedChunkRaster(this.waterComponent);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int waterType = waterRaster.getWaterType(localX, localZ);
                if (waterType != WaterRasterTile.LAND) {
                    int height = heightRaster.getShort(localX, localZ);
                    int waterLevel = waterRaster.getWaterLevel(localX, localZ);
                    if (height >= 0 && height < waterLevel) {
                        for (int localY = height + 1; localY <= waterLevel; localY++) {
                            pos.set(localX, localY, localZ);
                            chunk.setBlockState(pos, this.block, false);
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
