package net.gegy1000.earth.server.world.soil.horizon;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.Arrays;
import java.util.Random;

public final class EvenDistributionHorizonConfig implements SoilHorizonConfig {
    private final IBlockState[] states;

    private EvenDistributionHorizonConfig(IBlockState... states) {
        this.states = states;
    }

    public static SoilHorizonConfig of(IBlockState... states) {
        return new EvenDistributionHorizonConfig(states);
    }

    public static SoilHorizonConfig of(Block... blocks) {
        IBlockState[] states = Arrays.stream(blocks).map(Block::getDefaultState).toArray(IBlockState[]::new);
        return new EvenDistributionHorizonConfig(states);
    }

    @Override
    public IBlockState getState(int x, int z, int depth, Random random) {
        return this.states[random.nextInt(this.states.length)];
    }
}
