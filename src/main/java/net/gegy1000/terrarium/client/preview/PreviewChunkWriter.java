package net.gegy1000.terrarium.client.preview;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.gengen.api.ChunkPrimeWriter;
import net.gegy1000.gengen.api.CubicPos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PreviewChunkWriter implements ChunkPrimeWriter {
    private final CubicPos cubePos;
    private final char[] data = new char[4096];
    private int writeCount;

    public PreviewChunkWriter(CubicPos cubePos) {
        this.cubePos = cubePos;
    }

    @Override
    public void set(int x, int y, int z, IBlockState state) {
        if (y >= this.cubePos.getMinY() && y <= this.cubePos.getMaxY()) {
            char value = (char) Block.BLOCK_STATE_IDS.get(state);
            this.data[getIndex(x & 0xF, y & 0xF, z & 0xF)] = value;
            this.writeCount++;
        }
    }

    @Override
    public IBlockState get(int x, int y, int z) {
        if (y < this.cubePos.getMinY() || y > this.cubePos.getMaxY()) {
            return Blocks.AIR.getDefaultState();
        }
        IBlockState state = Block.BLOCK_STATE_IDS.getByValue(this.data[getIndex(x & 0xF, y & 0xF, z & 0xF)]);
        if (state == null) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    private static int getIndex(int x, int y, int z) {
        return x << 8 | z << 4 | y;
    }

    public PreviewChunkData build() {
        PreviewChunkData data = new PreviewChunkData(this.data);
        if (this.writeCount > 0) {
            data.setEmpty(this.computeEmpty());
            if (this.writeCount >= this.data.length) {
                data.setFilled(this.computeFilled());
            }
        } else {
            data.setEmpty(true);
        }
        return data;
    }

    private boolean computeEmpty() {
        char airId = (char) Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());
        for (char state : this.data) {
            if (state != airId) {
                return false;
            }
        }
        return true;
    }

    private boolean computeFilled() {
        char airId = (char) Block.BLOCK_STATE_IDS.get(Blocks.AIR.getDefaultState());
        for (char state : this.data) {
            if (state == airId) {
                return false;
            }
        }
        return true;
    }
}
