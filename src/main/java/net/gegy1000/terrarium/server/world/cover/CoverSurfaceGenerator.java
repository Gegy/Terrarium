package net.gegy1000.terrarium.server.world.cover;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;

import java.util.Random;

public abstract class CoverSurfaceGenerator<T extends CoverGenerationContext> extends CoverGenerator<T> {
    protected CoverSurfaceGenerator(T context, CoverType<T> coverType) {
        super(context, coverType);
    }

    public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
        this.iterateChunk((localX, localZ) -> {
            Biome biome = this.coverType.getBiome(this.context, originX + localX, originZ + localZ);
            coverBlockBuffer[localX + localZ * 16] = biome.topBlock;
        });
    }

    public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
        this.iterateChunk((localX, localZ) -> {
            Biome biome = this.coverType.getBiome(this.context, originX + localX, originZ + localZ);
            fillerBlockBuffer[localX + localZ * 16] = biome.fillerBlock;
        });
    }

    public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
    }

    protected void coverFromLayer(IBlockState[] blockBuffer, int originX, int originZ, GenLayer layer, BlockProvider blockProvider) {
        int[] sampledLayer = this.sampleChunk(layer, originX, originZ);

        this.iterateChunk((localX, localZ) -> {
            int bufferIndex = localX + localZ * 16;
            int sampledValue = sampledLayer[bufferIndex];

            blockBuffer[bufferIndex] = blockProvider.provideBlock(sampledValue, localX, localZ);
        });
    }

    protected void coverBlock(IBlockState[] blockBuffer, IBlockState state) {
        this.iterateChunk((localX, localZ) -> blockBuffer[localX + localZ * 16] = state);
    }

    protected interface BlockProvider {
        IBlockState provideBlock(int sampledValue, int localX, int localZ);
    }

    public static class Static<T extends CoverGenerationContext> extends CoverSurfaceGenerator<T> {
        private final IBlockState coverState;
        private final IBlockState fillerState;

        public Static(T context, CoverType<T> coverType, IBlockState coverState, IBlockState fillerState) {
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
    }

    public static class Inherit<T extends CoverGenerationContext> extends CoverSurfaceGenerator<T> {
        public Inherit(T context, CoverType<T> coverType) {
            super(context, coverType);
        }
    }
}
