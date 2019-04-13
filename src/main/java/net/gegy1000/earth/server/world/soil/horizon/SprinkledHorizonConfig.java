package net.gegy1000.earth.server.world.soil.horizon;

import net.minecraft.block.state.IBlockState;

import java.util.Random;

public final class SprinkledHorizonConfig implements SoilHorizonConfig {
    private final SoilHorizonConfig horizonConfig;
    private final IBlockState sprinkle;
    private final int sprinkleProbability;

    private SprinkledHorizonConfig(SoilHorizonConfig horizonConfig, IBlockState sprinkle, int sprinkleProbability) {
        this.horizonConfig = horizonConfig;
        this.sprinkle = sprinkle;
        this.sprinkleProbability = sprinkleProbability;
    }

    public static SoilHorizonConfig of(SoilHorizonConfig horizonConfig, IBlockState sprinkle, int sprinkleProbability) {
        return new SprinkledHorizonConfig(horizonConfig, sprinkle, sprinkleProbability);
    }

    @Override
    public IBlockState getState(int x, int z, int depth, Random random) {
        if (random.nextInt(this.sprinkleProbability) == 0) {
            return this.sprinkle;
        }
        return this.horizonConfig.getState(x, z, depth, random);
    }
}
