package net.gegy1000.earth.server.world.soil.horizon;

import net.minecraft.block.state.IBlockState;

import java.util.Random;

public class MixedClayHorizonConfig implements SoilHorizonConfig {
    private final IBlockState mixed;
    private final IBlockState clay;
    private final int mixedWeight;

    public MixedClayHorizonConfig(IBlockState mixed, IBlockState clay, int mixedWeight) {
        this.mixed = mixed;
        this.clay = clay;
        this.mixedWeight = mixedWeight;
    }

    public MixedClayHorizonConfig(IBlockState mixed, IBlockState clay) {
        this(mixed, clay, 3);
    }

    @Override
    public IBlockState getState(int x, int z, int depth, Random random) {
        int clayWeight = Math.min((depth / 2) + 1, 3);
        if (random.nextInt(this.mixedWeight + clayWeight) < this.mixedWeight) {
            return this.mixed;
        } else {
            return this.clay;
        }
    }
}
