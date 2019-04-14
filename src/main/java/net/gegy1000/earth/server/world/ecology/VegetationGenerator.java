package net.gegy1000.earth.server.world.ecology;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public interface VegetationGenerator {
    void generate(World world, Random random, BlockPos pos);
}
