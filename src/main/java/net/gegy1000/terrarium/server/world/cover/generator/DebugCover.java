package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Random;

public class DebugCover extends CoverGenerator {
    public DebugCover(CoverType type) {
        super(type);
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z, int slope) {
        return Blocks.QUARTZ_BLOCK.getDefaultState();
    }

    @Override
    protected IBlockState getFillerAt(Random random, int x, int z, int slope) {
        return Blocks.QUARTZ_BLOCK.getDefaultState();
    }
}
