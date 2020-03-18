package net.gegy1000.earth.server.world.feature;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

public class FlowersFeature extends WorldGenerator {
    private final IBlockState flowerState;
    private final IPlantable flower;

    public FlowersFeature(IBlockState flowerState) {
        this.flowerState = flowerState;
        if (flowerState.getBlock() instanceof IPlantable) {
            this.flower = (IPlantable) flowerState.getBlock();
        } else {
            this.flower = Blocks.YELLOW_FLOWER;
        }
    }

    @Override
    public boolean generate(World world, Random random, BlockPos origin) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        int count = random.nextInt(64);
        int worldHeight = world.getHeight();

        for (int i = 0; i < count; i++) {
            mutablePos.setPos(
                    origin.getX() + random.nextInt(8) - random.nextInt(8),
                    origin.getY() + random.nextInt(4) - random.nextInt(4),
                    origin.getZ() + random.nextInt(8) - random.nextInt(8)
            );

            if (mutablePos.getY() >= worldHeight) {
                continue;
            }

            if (world.isAirBlock(mutablePos)) {
                BlockPos groundPos = mutablePos.down();
                IBlockState ground = world.getBlockState(groundPos);
                if (ground.getBlock().canSustainPlant(ground, world, groundPos, EnumFacing.UP, this.flower)) {
                    world.setBlockState(mutablePos, this.flowerState, 2);
                }
            }
        }

        return true;
    }
}
