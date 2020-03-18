package net.gegy1000.earth.server.world.feature;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class TallFlowerFeature extends WorldGenerator {
    private final BlockDoublePlant.EnumPlantType type;

    public TallFlowerFeature(BlockDoublePlant.EnumPlantType type) {
        this.type = type;
    }

    @Override
    public boolean generate(World world, Random random, BlockPos origin) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        int count = random.nextInt(64);
        int worldHeight = world.getHeight();

        for (int i = 0; i < count; ++i) {
            mutablePos.setPos(
                    origin.getX() + random.nextInt(8) - random.nextInt(8),
                    origin.getY() + random.nextInt(4) - random.nextInt(4),
                    origin.getZ() + random.nextInt(8) - random.nextInt(8)
            );

            if (mutablePos.getY() + 1 >= worldHeight) {
                continue;
            }

            if (world.isAirBlock(mutablePos)) {
                BlockPos groundPos = mutablePos.down();
                IBlockState ground = world.getBlockState(groundPos);
                if (ground.getBlock().canSustainPlant(ground, world, groundPos, EnumFacing.UP, Blocks.DOUBLE_PLANT)) {
                    Blocks.DOUBLE_PLANT.placeAt(world, mutablePos, this.type, 2);
                }
            }
        }

        return true;
    }
}
