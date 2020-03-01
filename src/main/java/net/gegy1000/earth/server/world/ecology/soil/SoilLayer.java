package net.gegy1000.earth.server.world.ecology.soil;

import net.gegy1000.terrarium.server.util.WeightedPool;
import net.minecraft.block.state.IBlockState;

import java.util.Random;

public interface SoilLayer {
    static SoilLayer uniform(IBlockState state) {
        return random -> state;
    }

    static SoilLayer random(IBlockState... states) {
        return random -> states[random.nextInt(states.length)];
    }

    static SoilLayer random(WeightedPool<IBlockState> pool) {
        return pool::sampleOrExcept;
    }

    IBlockState sample(Random random);
}
