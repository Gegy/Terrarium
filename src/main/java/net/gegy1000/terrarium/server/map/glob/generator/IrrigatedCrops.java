package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.block.state.IBlockState;

import java.util.Random;

public class IrrigatedCrops extends Cropland {
    public IrrigatedCrops() {
        super(GlobType.IRRIGATED_CROPS);
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z) {
        if (x % 9 == 0 && z % 9 == 0) {
            return WATER;
        }
        if (random.nextInt(60) == 0) {
            return COARSE_DIRT;
        }
        return FARMLAND;
    }
}
