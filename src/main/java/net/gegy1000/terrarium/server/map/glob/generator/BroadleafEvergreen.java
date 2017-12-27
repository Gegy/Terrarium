package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.Random;

public class BroadleafEvergreen extends Forest {
    public BroadleafEvergreen() {
        super(GlobType.BROADLEAF_EVERGREEN);
    }

    @Override
    public void decorate(Random random, int x, int z) {
        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);

        this.decorateScatter(random, clearingLayer, x, z, this.range(random, 2, 4), pos -> {
            int height = this.range(random, 3, 6);
            if (random.nextInt(10) == 0) {
                height += 3;
            }
            new WorldGenTrees(false, height, OAK_LOG, OAK_LEAF, false).generate(this.world, random, pos);
        });

        this.decorateScatter(random, clearingLayer, x, z, this.range(random, 0, 1), pos -> {
            int height = this.range(random, 5, 10);
            new WorldGenTrees(false, height, JUNGLE_LOG, JUNGLE_LEAF, true).generate(this.world, random, pos);
        });
    }
}
