package net.gegy1000.earth.server.world.feature;

import net.gegy1000.earth.server.world.ecology.SoilPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class FloorShrubFeature extends AbstractTreeFeature {
    public FloorShrubFeature(boolean notify, IBlockState log, IBlockState leaves) {
        super(notify, log, leaves);
    }

    @Override
    public boolean generate(World world, Random random, BlockPos origin) {
        if (!this.tryGrowOn(world, origin.down(), SoilPredicate.ANY)) {
            return false;
        }

        this.setLog(world, origin);
        this.setLeaves(world, random, origin);

        return true;
    }

    private void setLeaves(World world, Random random, BlockPos origin) {
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(origin);

        int height = 2;

        for (int y = origin.getY(); y <= origin.getY() + height; y++) {
            int deltaY = y - origin.getY();
            int radius = height - deltaY;

            for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                int deltaZ = z - origin.getZ();

                for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
                    int deltaX = x - origin.getX();

                    if (Math.abs(deltaX) != radius || Math.abs(deltaZ) != radius || random.nextInt(2) != 0) {
                        mutablePos.setPos(x, y, z);
                        this.trySetLeaves(world, mutablePos);
                    }
                }
            }
        }
    }
}
