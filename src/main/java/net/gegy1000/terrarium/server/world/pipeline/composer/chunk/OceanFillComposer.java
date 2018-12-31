package net.gegy1000.terrarium.server.world.pipeline.composer.chunk;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.BlockState;
import net.minecraft.class_2919;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class OceanFillComposer<C extends TerrariumGeneratorConfig> implements ChunkComposer<C> {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final BlockState block;
    private final int oceanLevel;

    public OceanFillComposer(RegionComponentType<ShortRasterTile> heightComponent, BlockState block, int oceanLevel) {
        this.heightComponent = heightComponent;
        this.block = block;
        this.oceanLevel = oceanLevel;
    }

    @Override
    public void compose(ChunkGenerator<C> generator, Chunk chunk, class_2919 random, RegionGenerationHandler regionHandler) {
        ShortRasterTile heightRaster = regionHandler.getCachedChunkRaster(this.heightComponent);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = heightRaster.getShort(localX, localZ);
                if (height < this.oceanLevel) {
                    for (int localY = height + 1; localY <= this.oceanLevel; localY++) {
                        pos.set(localX, localY, localZ);
                        chunk.setBlockState(pos, this.block, false);
                    }
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.heightComponent };
    }
}
