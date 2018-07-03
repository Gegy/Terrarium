package net.gegy1000.terrarium.server.world.cover.generator.primer;

import net.minecraft.block.state.IBlockState;

public interface CoverPrimer {
    void setBlockState(int x, int y, int z, IBlockState state);

    IBlockState getBlockState(int x, int y, int z);
}
