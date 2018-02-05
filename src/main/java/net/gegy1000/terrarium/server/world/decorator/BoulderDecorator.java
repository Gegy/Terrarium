package net.gegy1000.terrarium.server.world.decorator;

import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.BoulderGenerator;
import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BoulderDecorator implements TerrariumWorldDecorator {
    private static final BoulderGenerator BOULDER_GENERATOR = new BoulderGenerator(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), 0);

    @Override
    public void decorate(World world, Random random, CoverType[] coverBuffer, byte[] slopeBuffer, int x, int z) {
        for (int i = 0; i < 2; i++) {
            int localX = random.nextInt(16);
            int localZ = random.nextInt(16);

            if (random.nextInt(8) == 0) {
                if ((slopeBuffer[localX + localZ * 16] & 0xFF) >= CoverGenerator.MOUNTAINOUS_SLOPE || random.nextInt(60) == 0) {
                    int spawnX = localX + x + 8;
                    int spawnZ = localZ + z + 8;

                    BOULDER_GENERATOR.generate(world, random, world.getTopSolidOrLiquidBlock(new BlockPos(spawnX, 0, spawnZ)));
                }
            }
        }
    }
}
