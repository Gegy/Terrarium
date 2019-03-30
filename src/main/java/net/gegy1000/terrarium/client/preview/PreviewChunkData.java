package net.gegy1000.terrarium.client.preview;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

public class PreviewChunkData {
    private final char[] data;
    private boolean empty;
    private boolean filled;

    public PreviewChunkData(char[] data) {
        this.data = data;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public boolean isFilled() {
        return this.filled;
    }

    public IBlockState get(int x, int y, int z) {
        IBlockState state = Block.BLOCK_STATE_IDS.getByValue(this.data[getIndex(x, y, z)]);
        if (state == null) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    private static int getIndex(int x, int y, int z) {
        return x << 8 | z << 4 | y;
    }
}
