package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.Random;

public class OpenBroadleafDeciduous extends Forest {
    public OpenBroadleafDeciduous() {
        super(GlobType.OPEN_BROADLEAF_DECIDUOUS);
    }

    @Override
    public void decorate(Random random, int x, int z) {
        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);

        this.decorateScatter(random, clearingLayer, x, z, this.range(random, 3, 6), pos -> {
            if (random.nextInt(10) != 0) {
                int height = this.range(random, 3, 6);
                if (random.nextInt(10) == 0) {
                    height += 3;
                }
                new WorldGenTrees(false, height, Forest.OAK_LOG, Forest.OAK_LEAF, false).generate(this.world, random, pos);
            } else {
                int height = this.range(random, 5, 10);
                new WorldGenTrees(false, height, Forest.JUNGLE_LOG, Forest.JUNGLE_LEAF, true).generate(this.world, random, pos);
            }
        });
    }
}
