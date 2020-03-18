package net.gegy1000.earth.server.world.feature;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

public class TallShrubFeature extends AbstractTreeFeature {
    public TallShrubFeature(boolean notify, IBlockState log, IBlockState leaves) {
        super(notify, log, leaves);
    }

    @Override
    public boolean generate(World world, Random random, BlockPos origin) {
        if (!this.tryGrowOn(world, origin.down(), (IPlantable) Blocks.SAPLING)) {
            return false;
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(origin);

        this.setWood(world, origin, mutablePos);

        boolean thickLeaves = random.nextBoolean();
        if (thickLeaves) {
            this.setLeafPlane(world, origin.up(1), mutablePos);
            this.setLeafCross(world, origin.up(2), mutablePos);
        } else {
            this.setLeafCross(world, origin.up(1), mutablePos);
            this.setLeaves(world, origin.up(2));
        }

        return true;
    }

    private void setWood(World world, BlockPos origin, BlockPos.MutableBlockPos mutablePos) {
        for (int y = 0; y < 2; y++) {
            mutablePos.setY(origin.getY() + y);
            this.setLog(world, mutablePos);
        }
    }

    private void setLeafPlane(World world, BlockPos origin, BlockPos.MutableBlockPos mutablePos) {
        for (int z = -1; z <= 1; z++) {
            for (int x = -1; x <= 1; x++) {
                mutablePos.setPos(origin.getX() + x, origin.getY(), origin.getZ() + z);
                this.trySetLeaves(world, mutablePos);
            }
        }
    }

    private void setLeafCross(World world, BlockPos origin, BlockPos.MutableBlockPos mutablePos) {
        for (int z = -1; z <= 1; z++) {
            mutablePos.setPos(origin.getX(), origin.getY(), origin.getZ() + z);
            this.trySetLeaves(world, mutablePos);
        }

        for (int x = -1; x <= 1; x++) {
            mutablePos.setPos(origin.getX() + x, origin.getY(), origin.getZ());
            this.trySetLeaves(world, mutablePos);
        }
    }
}
