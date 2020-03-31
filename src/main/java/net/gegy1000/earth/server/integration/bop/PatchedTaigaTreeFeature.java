package net.gegy1000.earth.server.integration.bop;

import biomesoplenty.api.block.IBlockPosQuery;
import biomesoplenty.common.util.biome.GeneratorUtils;
import biomesoplenty.common.world.generator.tree.GeneratorTaigaTree;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class PatchedTaigaTreeFeature extends GeneratorTaigaTree {
    public static class Builder extends GeneratorTaigaTree.Builder {
        @Override
        public PatchedTaigaTreeFeature create() {
            return new PatchedTaigaTreeFeature(
                    this.amountPerChunk,
                    this.placeOn, this.replace,
                    this.log, this.leaves,
                    this.vine, this.hanging,
                    this.trunkFruit, this.altLeaves,
                    this.minHeight, this.maxHeight, this.trunkWidth,
                    this.scatterYMethod
            );
        }
    }

    private final int trunkWidth;

    PatchedTaigaTreeFeature(
            float amountPerChunk,
            IBlockPosQuery placeOn, IBlockPosQuery replace,
            IBlockState log, IBlockState leaves,
            IBlockState vine, IBlockState hanging,
            IBlockState trunkFruit, IBlockState altLeaves,
            int minHeight, int maxHeight, int trunkWidth,
            GeneratorUtils.ScatterYMethod scatterYMethod
    ) {
        super(amountPerChunk, placeOn, replace, log, leaves, vine, hanging, trunkFruit, altLeaves, minHeight, maxHeight, trunkWidth, scatterYMethod);
        this.trunkWidth = trunkWidth;
    }

    @Override
    public boolean checkSpace(World world, BlockPos pos, int baseHeight, int height) {
        int worldHeight = world.getHeight() - 1;

        for (int y = 0; y <= height; y++) {
            int trunkWidth = (this.trunkWidth * (height - y) / height) + 1;
            int trunkStart = MathHelper.ceil(0.25 - trunkWidth / 2.0);
            int trunkEnd = MathHelper.floor(0.25 + trunkWidth / 2.0);

            int start = (y <= baseHeight ? trunkStart : trunkStart - 1);
            int end = (y <= baseHeight ? trunkEnd : trunkEnd + 1);

            for (int z = start; z <= end; z++) {
                for (int x = start; x <= end; x++) {
                    BlockPos trunkPos = pos.add(x, y, z);
                    if (trunkPos.getY() >= worldHeight || !this.replace.matches(world, trunkPos)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
