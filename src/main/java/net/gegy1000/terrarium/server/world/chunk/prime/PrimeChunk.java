package net.gegy1000.terrarium.server.world.chunk.prime;

import net.gegy1000.terrarium.server.world.chunk.ComposingChunk;
import net.minecraft.block.state.IBlockState;

public interface PrimeChunk extends ComposingChunk {
    void set(int x, int y, int z, IBlockState state);

    IBlockState get(int x, int y, int z);
}
