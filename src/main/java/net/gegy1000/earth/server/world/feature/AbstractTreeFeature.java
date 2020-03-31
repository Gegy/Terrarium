package net.gegy1000.earth.server.world.feature;

import net.gegy1000.earth.server.world.ecology.SoilPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public abstract class AbstractTreeFeature extends WorldGenerator {
    public final IBlockState log;
    public final IBlockState leaves;

    public AbstractTreeFeature(boolean notify, IBlockState log, IBlockState leaves) {
        super(notify);
        this.log = log;
        this.leaves = leaves;
    }

    protected void setLog(World world, BlockPos pos) {
        this.setBlockAndNotifyAdequately(world, pos, this.log);
    }

    protected void setLeaves(World world, BlockPos pos) {
        this.setBlockAndNotifyAdequately(world, pos, this.leaves);
    }

    protected boolean trySetLeaves(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock().canBeReplacedByLeaves(state, world, pos) || !state.isFullBlock()) {
            this.setLeaves(world, pos);
            return true;
        }
        return false;
    }

    protected boolean tryGrowOn(World world, BlockPos soilPos, SoilPredicate soilPredicate) {
        IBlockState soilState = world.getBlockState(soilPos);
        Block soilBlock = soilState.getBlock();

        if (soilPredicate.canGrowOn(world, soilPos, soilState)) {
            soilBlock.onPlantGrow(soilState, world, soilPos, soilPos.up());
            return true;
        }

        return false;
    }
}
