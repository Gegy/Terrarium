package net.gegy1000.terrarium.server.map.glob.generator.primer;

import net.minecraft.block.state.IBlockState;

public interface GlobPrimer {
    void setBlockState(int x, int y, int z, IBlockState state);

    IBlockState getBlockState(int x, int y, int z);
}
