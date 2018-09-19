package net.gegy1000.terrarium.server.world.chunk;

import net.minecraft.block.state.IBlockState;

public interface ComposeChunk {
    void set(int x, int y, int z, IBlockState state);

    IBlockState get(int x, int y, int z);

    int getX();

    int getZ();

    int getMinY();

    default int getMaxY() {
        return this.getMinY() + this.getSizeY();
    }

    int getSizeY();
}
