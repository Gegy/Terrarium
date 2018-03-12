package net.gegy1000.terrarium.server.world.feature.tree;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.Random;

public class GenerousTreeGenerator extends WorldGenTrees {
    private final boolean beans;

    public GenerousTreeGenerator(boolean notify, int height, IBlockState wood, IBlockState leaves, boolean vines, boolean beans) {
        super(notify, height, wood, leaves, vines);
        this.beans = beans;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        IBlockState previousGround = world.getBlockState(position.down());
        boolean replacedGround = false;
        if (previousGround.getBlock() == Blocks.SAND) {
            world.setBlockState(position.down(), Blocks.DIRT.getDefaultState());
            replacedGround = true;
        }
        boolean result = super.generate(world, rand, position);
        if (replacedGround) {
            world.setBlockState(position.down(), previousGround);
        }
        return result;
    }

    @Override
    protected boolean canGrowInto(Block blockType) {
        if (super.canGrowInto(blockType)) {
            return true;
        }
        Material material = blockType.getDefaultState().getMaterial();
        return material == Material.PLANTS || material == Material.VINE;
    }

    @Override
    protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
        if (this.beans || state.getBlock() != Blocks.COCOA) {
            super.setBlockAndNotifyAdequately(world, pos, state);
        }
    }
}
