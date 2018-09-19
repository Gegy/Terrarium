package net.gegy1000.terrarium.server.world.chunk.populate;

import net.gegy1000.terrarium.server.world.chunk.CubicPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nullable;

public class ColumnPopulateChunk implements PopulateChunk {
    private final CubicPos pos;
    private final World world;

    public ColumnPopulateChunk(CubicPos pos, World world) {
        this.pos = pos;
        this.world = world;
    }

    @Override
    public CubicPos getPos() {
        return this.pos;
    }

    @Override
    public World getGlobal() {
        return this.world;
    }

    @Nullable
    @Override
    public BlockPos getSurface(int x, int z) {
        Chunk chunk = this.world.getChunk(this.pos.getX(), this.pos.getZ());

        int minY = this.pos.getMinY() + 8;

        BlockPos surfacePos = new BlockPos(x, this.pos.getMaxY() + 8, z);
        BlockPos nextPos;

        while (surfacePos.getY() >= minY) {
            nextPos = surfacePos.down();
            IBlockState state = chunk.getBlockState(nextPos);

            if (state.getMaterial().blocksMovement() && !state.getBlock().isLeaves(state, this.world, nextPos) && !state.getBlock().isFoliage(this.world, nextPos)) {
                return surfacePos;
            }

            surfacePos = nextPos;
        }

        return null;
    }
}
