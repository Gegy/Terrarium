package net.gegy1000.terrarium.server.world.chunk;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

public class ColumnComposeChunk implements ComposeChunk {
    private final int x;
    private final int z;
    private final ChunkPrimer primer;

    public ColumnComposeChunk(int x, int z, ChunkPrimer primer) {
        this.x = x;
        this.z = z;
        this.primer = primer;
    }

    @Override
    public void set(int x, int y, int z, IBlockState state) {
        this.primer.setBlockState(x, y, z, state);
    }

    @Override
    public IBlockState get(int x, int y, int z) {
        return this.primer.getBlockState(x, y, z);
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getMaxY() {
        return 256;
    }

    @Override
    public int getSizeY() {
        return 256;
    }
}
