package net.gegy1000.earth.server.world.feature;

import net.gegy1000.earth.server.world.ecology.SoilPredicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public final class HookGrowthCheckFeature extends WorldGenerator {
    private static final IBlockState UNIVERSAL_SOIL = Blocks.DIRT.getDefaultState();

    private final WorldGenerator inner;
    private final SoilPredicate soilPredicate;

    public HookGrowthCheckFeature(WorldGenerator inner, SoilPredicate soilPredicate) {
        this.inner = inner;
        this.soilPredicate = soilPredicate;
    }

    @Override
    public boolean generate(World world, Random random, BlockPos origin) {
        BlockPos soilPos = origin.down();
        IBlockState soilState = world.getBlockState(soilPos);
        if (!this.soilPredicate.canGrowOn(world, soilPos, soilState)) {
            return false;
        }

        try {
            world.setBlockState(soilPos, UNIVERSAL_SOIL, 18);
            return this.inner.generate(world, random, origin);
        } finally {
            world.setBlockState(soilPos, soilState, 18);
        }
    }
}
