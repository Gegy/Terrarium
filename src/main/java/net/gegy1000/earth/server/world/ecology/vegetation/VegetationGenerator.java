package net.gegy1000.earth.server.world.ecology.vegetation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public interface VegetationGenerator {
    static VegetationGenerator of(WorldGenerator generator) {
        return generator::generate;
    }

    void generate(World world, Random random, BlockPos pos);
}
