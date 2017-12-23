package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Random;

public class Snow extends GlobGenerator {
    private static final IBlockState SNOW = Blocks.SNOW.getDefaultState();

    public Snow() {
        super(GlobType.SNOW);
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z) {
        return SNOW;
    }

    @Override
    protected IBlockState getFillerAt(Random random, int x, int z) {
        return SNOW;
    }
}
