package net.gegy1000.terrarium.server.world.generator.tree;

import net.minecraft.block.BlockSapling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenShrub;

import java.util.Random;

public class GenerousDenseShrubGenerator extends WorldGenShrub {
    private final IBlockState wood;
    private final IBlockState leaves;

    public GenerousDenseShrubGenerator(IBlockState wood, IBlockState leaves) {
        super(wood, leaves);
        this.wood = wood;
        this.leaves = leaves;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        IBlockState ground = world.getBlockState(position);
        while ((ground.getBlock().isAir(ground, world, position) || ground.getBlock().isLeaves(ground, world, position)) && position.getY() > 0) {
            position = position.down();
            ground = world.getBlockState(position);
        }

        if (ground.getBlock().canSustainPlant(ground, world, position, EnumFacing.UP, (BlockSapling) Blocks.SAPLING)) {
            position = position.up();
            this.setBlockAndNotifyAdequately(world, position, this.wood);

            for (int y = position.getY(); y <= position.getY() + 2; ++y) {
                int height = y - position.getY();
                int size = 2 - height;
                this.generateLayer(world, rand, position, y, size);
            }
        }

        return true;
    }

    private void generateLayer(World world, Random rand, BlockPos position, int y, int size) {
        for (int x = position.getX() - size; x <= position.getX() + size; x++) {
            int deltaX = x - position.getX();
            for (int z = position.getZ() - size; z <= position.getZ() + size; z++) {
                int deltaZ = z - position.getZ();
                if (Math.abs(deltaX) != size || Math.abs(deltaZ) != size || rand.nextInt(2) != 0) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    Material material = state.getMaterial();
                    if (material == Material.PLANTS || material == Material.VINE || state.getBlock().canBeReplacedByLeaves(state, world, pos)) {
                        this.setBlockAndNotifyAdequately(world, pos, this.leaves);
                    }
                }
            }
        }
    }

    @Override
    protected void setBlockAndNotifyAdequately(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state, 3);
    }
}
