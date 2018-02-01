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

public class SmallShrubGenerator extends WorldGenAbstractTree {
    private final IBlockState wood;
    private final IBlockState leaves;

    public SmallShrubGenerator(IBlockState wood, IBlockState leaves) {
        super(true);
        this.wood = wood;
        this.leaves = leaves;
    }

    @Override
    public boolean generate(World world, Random random, BlockPos position) {
        int height = random.nextInt(2) + 1;

        if (position.getY() >= 1 && position.getY() + height + 1 < world.getHeight()) {
            BlockPos groundPos = position.down();
            IBlockState ground = world.getBlockState(groundPos);
            boolean onSoil = ground.getBlock().canSustainPlant(ground, world, groundPos, EnumFacing.UP, (BlockSapling) Blocks.SAPLING);

            if (onSoil) {
                ground.getBlock().onPlantGrow(ground, world, groundPos, position);

                for (int y = 0; y < height; y++) {
                    this.setBlockAndNotifyAdequately(world, position.up(y), this.wood);
                }
                this.setBlockAndNotifyAdequately(world, position.up(height), this.leaves);

                this.generateLeaves(world, random, position, height);

                return true;
            }
        }

        return false;
    }

    private void generateLeaves(World world, Random random, BlockPos position, int height) {
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
                if (Math.abs(x) != Math.abs(z) || random.nextInt(4) == 0) {
                    if (x == 0 && z == 0) {
                        continue;
                    }
                    BlockPos leafPosition = position.add(x, height - 1, z);
                    if (this.canGrowInto(world.getBlockState(leafPosition).getBlock())) {
                        this.setBlockAndNotifyAdequately(world, leafPosition, this.leaves);
                    }
                }
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
