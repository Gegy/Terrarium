package net.gegy1000.terrarium.server.world.chunk.prime;

import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class CubePrimeChunk implements PrimeChunk {
    private final CubicPos pos;
    private final CubePrimer primer;

    public CubePrimeChunk(CubicPos pos, CubePrimer primer) {
        this.pos = pos;
        this.primer = primer;
    }

    @Override
    public void set(int x, int y, int z, IBlockState state) {
        if (y >= this.pos.getMinY() && y <= this.pos.getMaxY()) {
            this.primer.setBlockState(x, y & 0xF, z, state);
        }
    }

    @Override
    public IBlockState get(int x, int y, int z) {
        if (y >= this.pos.getMinY() && y <= this.pos.getMaxY()) {
            return this.primer.getBlockState(x, y & 0xF, z);
        } else {
            return Blocks.AIR.getDefaultState();
        }
    }

    @Override
    public CubicPos getPos() {
        return this.pos;
    }
}
