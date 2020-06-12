package net.gegy1000.earth.server.world.ecology.soil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public interface SoilTexture {
    IBlockState sample(Random random, BlockPos pos, int slope, int depth);
}
