package net.gegy1000.earth.server.world.soil.horizon;

import net.minecraft.block.state.IBlockState;

import java.util.Random;

public interface SoilHorizonConfig {
    IBlockState getState(int x, int z, int depth, Random random);
}
