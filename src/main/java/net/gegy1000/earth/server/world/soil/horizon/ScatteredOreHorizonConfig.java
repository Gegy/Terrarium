package net.gegy1000.earth.server.world.soil.horizon;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.Arrays;
import java.util.Random;

public final class ScatteredOreHorizonConfig implements SoilHorizonConfig {
    private final IBlockState[] states;
    private final IBlockState ore;
    private final int oreProbability;

    private ScatteredOreHorizonConfig(IBlockState ore, int oreProbability, IBlockState... states) {
        this.ore = ore;
        this.oreProbability = oreProbability;
        this.states = states;
    }

    public static SoilHorizonConfig of(IBlockState ore, int oreProbability, IBlockState... states) {
        return new ScatteredOreHorizonConfig(ore, oreProbability, states);
    }

    public static SoilHorizonConfig of(Block ore, int oreProbability, Block... blocks) {
        IBlockState[] states = Arrays.stream(blocks).map(Block::getDefaultState).toArray(IBlockState[]::new);
        return new ScatteredOreHorizonConfig(ore.getDefaultState(), oreProbability, states);
    }

    @Override
    public IBlockState getState(int x, int z, int depth, Random random) {
        if (random.nextInt(this.oreProbability) == 0) {
            return this.ore;
        }
        return this.states[random.nextInt(this.states.length)];
    }
}
