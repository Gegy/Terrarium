package net.gegy1000.terrarium.server.world.chunk.prime;

import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.ChunkPrimer;

public class ColumnPrimeChunk implements PrimeChunk {
    private final CubicPos pos;
    private final ChunkPrimer primer;

    public ColumnPrimeChunk(CubicPos pos, ChunkPrimer primer) {
        this.pos = pos;
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
    public CubicPos getPos() {
        return this.pos;
    }
}
