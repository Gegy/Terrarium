package net.gegy1000.earth.server.world.soil.horizon;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

import java.util.Random;

public final class SimpleHorizonConfig implements SoilHorizonConfig {
    private final IBlockState state;

    private SimpleHorizonConfig(IBlockState state) {
        this.state = state;
    }

    public static SoilHorizonConfig of(IBlockState state) {
        return new SimpleHorizonConfig(state);
    }

    public static SoilHorizonConfig of(Block block) {
        return new SimpleHorizonConfig(block.getDefaultState());
    }

    @Override
    public IBlockState getState(int x, int z, int depth, Random random) {
        return this.state;
    }
}
