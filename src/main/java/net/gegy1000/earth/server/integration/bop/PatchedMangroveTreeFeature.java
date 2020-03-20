package net.gegy1000.earth.server.integration.bop;

import biomesoplenty.api.block.IBlockPosQuery;
import biomesoplenty.common.util.biome.GeneratorUtils;
import biomesoplenty.common.world.generator.tree.GeneratorMangroveTree;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PatchedMangroveTreeFeature extends GeneratorMangroveTree {

    public static class Builder extends GeneratorMangroveTree.Builder {
        @Override
        public PatchedMangroveTreeFeature create() {
            return new PatchedMangroveTreeFeature(
                    this.amountPerChunk,
                    this.placeOn, this.replace,
                    this.log, this.leaves,
                    this.vine, this.hanging, this.trunkFruit, this.altLeaves,
                    this.minHeight, this.maxHeight,
                    this.minLeavesRadius, this.leavesGradient,
                    this.vineAttempts, this.maxVineLength,
                    this.rootsReplace,
                    this.scatterYMethod
            );
        }
    }

    public PatchedMangroveTreeFeature(
            float amountPerChunk,
            IBlockPosQuery placeOn, IBlockPosQuery replace,
            IBlockState log, IBlockState leaves,
            IBlockState vine, IBlockState hanging, IBlockState trunkFruit, IBlockState altLeaves,
            int minHeight, int maxHeight,
            int minLeavesRadius, int leavesGradient,
            int vineAttempts, int maxVineLength,
            IBlockPosQuery rootsReplace,
            GeneratorUtils.ScatterYMethod scatterYMethod
    ) {
        super(
                amountPerChunk,
                placeOn, replace,
                log, leaves,
                vine, hanging, trunkFruit, altLeaves,
                minHeight, maxHeight,
                minLeavesRadius, leavesGradient,
                vineAttempts, maxVineLength,
                rootsReplace,
                scatterYMethod
        );
    }

    @Override
    public boolean checkSpace(World world, BlockPos pos, int rootHeight, int middleHeight, int height) {
        if (this.countViableRoots(world, pos, rootHeight) < 2) return false;

        // PATCH: for cubic chunks support
        if (pos.getY() + height >= world.getHeight() - 1) {
            return false;
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        for (int y = rootHeight; y <= height; y++) {
            int radius = (y <= (rootHeight + middleHeight) ? 1 : 2);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    mutablePos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (!this.replace.matches(world, mutablePos)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int countViableRoots(World world, BlockPos pos, int rootHeight) {
        int rootsViable = 0;
        for (EnumFacing direction : EnumFacing.Plane.HORIZONTAL) {
            if (this.checkRootViable(world, pos, rootHeight, direction)) {
                rootsViable++;
            }
        }
        return rootsViable;
    }
}
