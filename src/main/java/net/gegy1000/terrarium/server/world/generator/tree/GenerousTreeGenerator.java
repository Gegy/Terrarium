package net.gegy1000.terrarium.server.world.generator.tree;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenTrees;

public class GenerousTreeGenerator extends WorldGenTrees {
    private final boolean beans;

    public GenerousTreeGenerator(boolean notify, int height, IBlockState wood, IBlockState leaves, boolean vines, boolean beans) {
        super(notify, height, wood, leaves, vines);
        this.beans = beans;
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
