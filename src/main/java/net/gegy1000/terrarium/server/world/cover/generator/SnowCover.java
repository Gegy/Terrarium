package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import java.util.Random;

public class SnowCover extends CoverGenerator {
    private static final IBlockState SNOW = Blocks.SNOW.getDefaultState();
    private static final IBlockState ICE = Blocks.PACKED_ICE.getDefaultState();

    public SnowCover() {
        super(CoverType.SNOW);
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z, int slope) {
        return slope >= EXTREME_CLIFF_SLOPE ? ICE : SNOW;
    }

    @Override
    protected IBlockState getFillerAt(Random random, int x, int z, int slope) {
        return this.getCoverAt(random, x, z, slope);
    }
}
