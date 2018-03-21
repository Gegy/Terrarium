package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.terrarium.server.world.cover.generator.primer.CoverPrimer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;

import java.util.Random;

public abstract class CoverSurfaceGenerator extends CoverGenerator {
    protected CoverSurfaceGenerator(CoverGenerationContext context, CoverType coverType) {
        super(context, coverType);
    }

    public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
        this.iterateChunk((localX, localZ) -> {
            coverBlockBuffer[localX + localZ * 16] = this.coverType.getBiome(originX + localX, originZ + localZ).topBlock;
        });
    }

    public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
        this.iterateChunk((localX, localZ) -> {
            fillerBlockBuffer[localX + localZ * 16] = this.coverType.getBiome(originX + localX, originZ + localZ).fillerBlock;
        });
    }

    public void decorate(int originX, int originZ, CoverPrimer primer, Random random) {
    }

    protected void coverFromLayer(IBlockState[] blockBuffer, int originX, int originZ, GenLayer layer, BlockProvider blockProvider) {
        ByteRasterTileAccess slopeRaster = this.context.getSlopeRaster();
        int[] sampledLayer = this.sampleChunk(layer, originX, originZ);

        this.iterateChunk((localX, localZ) -> {
            int bufferIndex = localX + localZ * 16;
            int sampledValue = sampledLayer[bufferIndex];
            int slope = slopeRaster.getUnsigned(localX, localZ);

            // TODO: This should be Earth-specific implementation
            IBlockState state = blockProvider.provideBlock(sampledValue, slope);
            if (slope >= CLIFF_SLOPE) {
                if (state == GRASS || state == PODZOL) {
                    state = COARSE_DIRT;
                } else if (state == COARSE_DIRT) {
                    state = COBBLESTONE;
                } else if (state == SAND) {
                    state = SANDSTONE;
                }
            }

            blockBuffer[bufferIndex] = state;
        });
    }

    protected void coverBlock(IBlockState[] blockBuffer, IBlockState state) {
        this.iterateChunk((localX, localZ) -> {
            blockBuffer[localX + localZ * 16] = state;
        });
    }

    protected interface BlockProvider {
        IBlockState provideBlock(int sampledValue, int slope);
    }

    public static class Static extends CoverSurfaceGenerator {
        private final IBlockState coverState;
        private final IBlockState fillerState;

        public Static(CoverGenerationContext context, CoverType coverType, IBlockState coverState, IBlockState fillerState) {
            super(context, coverType);
            this.coverState = coverState;
            this.fillerState = fillerState;
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.iterateChunk((localX, localZ) -> coverBlockBuffer[localX + localZ * 16] = this.coverState);
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            this.iterateChunk((localX, localZ) -> fillerBlockBuffer[localX + localZ * 16] = this.fillerState);
        }

        @Override
        public void decorate(int originX, int originZ, CoverPrimer primer, Random random) {
        }
    }

    public static class Inherit extends CoverSurfaceGenerator {
        public Inherit(CoverGenerationContext context, CoverType coverType) {
            super(context, coverType);
        }
    }
}
