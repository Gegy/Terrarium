package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.Random;

public class ClosedNeedleleafEvergreen extends Forest {
    public ClosedNeedleleafEvergreen() {
        super(GlobType.CLOSED_NEEDLELEAF_EVERGREEN);
    }

    @Override
    public void decorate(Random random, int x, int z) {
        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);

        this.decorateScatter(random, clearingLayer, x, z, this.range(random, 6, 8), pos -> {
            if (random.nextInt(8) != 0) {
                if (random.nextInt(3) == 0) {
                    TAIGA_1.generate(this.world, random, pos);
                } else {
                    TAIGA_2.generate(this.world, random, pos);
                }
            } else {
                int height = this.range(random, 5, 10);
                new WorldGenTrees(false, height, JUNGLE_LOG, JUNGLE_LEAF, true).generate(this.world, random, pos);
            }
        });
    }
}
