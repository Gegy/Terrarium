package net.gegy1000.earth.server.world.feature;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.IPlantable;

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

    protected boolean tryGrowOn(World world, BlockPos soilPos, IPlantable plant) {
        IBlockState soilState = world.getBlockState(soilPos);
        Block soilBlock = soilState.getBlock();
        if (soilBlock.canSustainPlant(soilState, world, soilPos, EnumFacing.UP, plant)) {
            soilBlock.onPlantGrow(soilState, world, soilPos, soilPos.up());
            return true;
        }
        return false;
    }
}
