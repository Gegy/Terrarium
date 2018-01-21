package net.gegy1000.terrarium.server.map.cover.generator.primer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

public class GlobChunkPrimer implements GlobPrimer {
    private final ChunkPrimer inner;

    public GlobChunkPrimer(ChunkPrimer inner) {
        this.inner = inner;
    }

    @Override
    public void setBlockState(int x, int y, int z, IBlockState state) {
        this.inner.setBlockState(x, y, z, state);
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return this.inner.getBlockState(x, y, z);
    }
}
