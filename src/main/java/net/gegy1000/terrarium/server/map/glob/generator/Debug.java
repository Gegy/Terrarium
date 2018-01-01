package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Random;

public class Debug extends GlobGenerator {
    public Debug() {
        super(GlobType.DEBUG);
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z) {
        return Blocks.REDSTONE_BLOCK.getDefaultState();
    }
}
