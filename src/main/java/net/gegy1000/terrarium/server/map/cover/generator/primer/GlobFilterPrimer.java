package net.gegy1000.terrarium.server.map.cover.generator.primer;

import net.minecraft.block.state.IBlockState;

public class GlobFilterPrimer implements GlobPrimer {
    private final GlobPrimer parent;
    private final int[] sampledTypes;
    private final int type;

    public GlobFilterPrimer(GlobPrimer parent, int[] sampledTypes, int type) {
        this.parent = parent;
        this.sampledTypes = sampledTypes;
        this.type = type;
    }

    @Override
    public void setBlockState(int x, int y, int z, IBlockState state) {
        if (this.sampledTypes[x + z * 16] == this.type) {
            this.parent.setBlockState(x, y, z, state);
        }
    }

    @Override
    public IBlockState getBlockState(int x, int y, int z) {
        return this.parent.getBlockState(x, y, z);
    }
}
