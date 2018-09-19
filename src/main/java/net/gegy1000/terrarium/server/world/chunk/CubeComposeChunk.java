package net.gegy1000.terrarium.server.world.chunk;

import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class CubeComposeChunk implements ComposeChunk {
    private final int x;
    private final int y;
    private final int z;
    private final CubePrimer primer;

    public CubeComposeChunk(int x, int y, int z, CubePrimer primer) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.primer = primer;
    }

    @Override
    public void set(int x, int y, int z, IBlockState state) {
        int minY = this.y << 4;
        if (y >= minY && y < minY + 16) {
            this.primer.setBlockState(x, y & 0xF, z, state);
        }
    }

    @Override
    public IBlockState get(int x, int y, int z) {
        int minY = this.y << 4;
        if (y >= minY && y < minY + 16) {
            return this.primer.getBlockState(x, y & 0xF, z);
        } else {
            return Blocks.AIR.getDefaultState();
        }
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
        return this.y << 4;
    }

    @Override
    public int getSizeY() {
        return 16;
    }
}
