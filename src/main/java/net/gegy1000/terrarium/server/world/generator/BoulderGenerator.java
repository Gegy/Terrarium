package net.gegy1000.terrarium.server.world.generator;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class BoulderGenerator extends WorldGenerator {
    private final IBlockState state;
    private final int baseRadius;

    public BoulderGenerator(IBlockState state, int baseRadius) {
        super(false);
        this.state = state;
        this.baseRadius = baseRadius;
    }

    @Override
    public boolean generate(World world, Random rand, BlockPos position) {
        while (world.isAirBlock(position.down())) {
            if (position.getY() <= 3) {
                return false;
            }
            position = position.down();
        }

        for (int i = 0; i < 3; ++i) {
            this.generateBlob(world, rand, position);

            int offsetX = -(this.baseRadius + 1) + rand.nextInt(2 + this.baseRadius * 2);
            int offsetY = -rand.nextInt(2);
            int offsetZ = -(this.baseRadius + 1) + rand.nextInt(2 + this.baseRadius * 2);
            position = position.add(offsetX, offsetY, offsetZ);
        }

        return true;
    }

    private void generateBlob(World world, Random rand, BlockPos position) {
        int sizeX = this.baseRadius + rand.nextInt(2);
        int sizeY = this.baseRadius + rand.nextInt(2);
        int sizeZ = this.baseRadius + rand.nextInt(2);
        double range = (sizeX + sizeY + sizeZ) * 0.333 + 0.5;

        for (BlockPos pos : BlockPos.getAllInBox(position.add(-sizeX, -sizeY, -sizeZ), position.add(sizeX, sizeY, sizeZ))) {
            if (pos.distanceSq(position) <= range * range) {
                world.setBlockState(pos, this.state, 4);
            }
        }
    }
}
