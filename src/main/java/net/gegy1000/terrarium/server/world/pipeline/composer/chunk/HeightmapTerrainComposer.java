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

public class HeightmapTerrainComposer<C extends TerrariumGeneratorConfig> implements TerrainNoiseComposer<C> {
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final BlockState block;

    public HeightmapTerrainComposer(RegionComponentType<ShortRasterTile> heightComponent, BlockState block) {
        this.heightComponent = heightComponent;
        this.block = block;
    }

    @Override
    public void compose(ChunkGenerator<C> generator, Chunk chunk, class_2919 random, RegionGenerationHandler regionHandler) {
        ShortRasterTile heightTile = regionHandler.getCachedChunkRaster(this.heightComponent);

        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int height = heightTile.getShort(localX, localZ);
                for (int localY = 1; localY <= height; localY++) {
                    pos.set(localX, localY, localZ);
                    chunk.setBlockState(pos, this.block, false);
                }
            }
        }
    }

    @Override
    public int sampleHeight(RegionGenerationHandler regionHandler, int x, int z) {
        ShortRasterTile heightTile = regionHandler.getCachedChunkRaster(this.heightComponent);
        return heightTile.getShort(x & 15, z & 15);
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.heightComponent };
    }
}
