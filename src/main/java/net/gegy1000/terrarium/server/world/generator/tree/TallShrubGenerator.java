package net.gegy1000.terrarium.server.world.generator.tree;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

import java.util.Random;

public class TallShrubGenerator extends WorldGenAbstractTree {
    private final IBlockState log;
    private final IBlockState leaves;

    public TallShrubGenerator(IBlockState log, IBlockState leaves) {
        super(false);
        this.log = log;
        this.leaves = leaves;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        int height = rand.nextInt(2) + 3;
        int leavesOrigin = height - rand.nextInt(2) - 1;
        int baseHeight = 1;

        if (position.getY() >= 1 && position.getY() + height + 1 <= 256) {
            for (int y = position.getY(); y <= position.getY() + 1 + height; y++) {
                int intersectionRange = y - position.getY() < baseHeight ? 0 : 3;
                if (this.checkIntersection(world, position, y, intersectionRange)) {
                    return false;
                }
            }

            BlockPos down = position.down();
            IBlockState state = world.getBlockState(down);
            boolean onSoil = state.getBlock().canSustainPlant(state, world, down, EnumFacing.UP, (BlockSapling) Blocks.SAPLING);

            if (onSoil && position.getY() < 256 - height - 1) {
                state.getBlock().onPlantGrow(state, world, down, position);

                this.generateLeaves(world, position, leavesOrigin, height, baseHeight);
                this.generateTrunk(world, position, height);

                return true;
            }
        }

        return false;
    }

    private void generateLeaves(World world, BlockPos position, int leavesOrigin, int height, int baseHeight) {
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
                this.tryGenerateLeaves(world, position, position.add(x, leavesOrigin, z), height, baseHeight);
            }
        }

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                this.tryGenerateLeaves(world, position, position.add(x, leavesOrigin + y, 0), height, baseHeight);
            }
            for (int z = -1; z <= 1; z++) {
                this.tryGenerateLeaves(world, position, position.add(0, leavesOrigin + y, z), height, baseHeight);
            }
        }

        this.tryGenerateLeaves(world, position, position.up(height), height, baseHeight);
    }

    private boolean checkIntersection(World world, BlockPos position, int y, int intersectionRange) {
        BlockPos.MutableBlockPos intersectionPos = new BlockPos.MutableBlockPos();

        for (int offsetX = position.getX() - intersectionRange; offsetX <= position.getX() + intersectionRange; ++offsetX) {
            for (int offsetZ = position.getZ() - intersectionRange; offsetZ <= position.getZ() + intersectionRange; ++offsetZ) {
                if (y < 0 || y > 255 || !this.isReplaceable(world, intersectionPos.setPos(offsetX, y, offsetZ))) {
                    return true;
                }
            }
        }

        return false;
    }

    private void tryGenerateLeaves(World world, BlockPos origin, BlockPos pos, int height, int baseHeight) {
        int offsetY = pos.getY() - origin.getY();
        if (offsetY <= height + 1 && offsetY >= baseHeight) {
            IBlockState state = world.getBlockState(pos);
            if ((pos.getX() != origin.getX() || pos.getZ() != origin.getZ() || offsetY >= height) && state.getBlock().canBeReplacedByLeaves(state, world, pos)) {
                this.setBlockAndNotifyAdequately(world, pos, this.leaves);
            }
        }
    }

    private void generateTrunk(World world, BlockPos root, int height) {
        for (int y = 0; y < height; y++) {
            BlockPos trunkPos = root.up(y);

            IBlockState state = world.getBlockState(trunkPos);
            if (state.getBlock().isAir(state, world, trunkPos) || state.getBlock().isLeaves(state, world, trunkPos)) {
                this.setBlockAndNotifyAdequately(world, root.up(y), this.log);
            }
        }
    }

    @Override
    protected boolean canGrowInto(Block blockType) {
        if (super.canGrowInto(blockType)) {
            return true;
        }
        Material material = blockType.getDefaultState().getMaterial();
        return material == Material.PLANTS || material == Material.VINE;
    }
}
