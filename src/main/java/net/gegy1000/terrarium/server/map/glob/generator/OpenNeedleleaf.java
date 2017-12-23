package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.Random;

public class OpenNeedleleaf extends Forest {
    public OpenNeedleleaf() {
        super(GlobType.OPEN_NEEDLELEAF);
    }

    @Override
    public void decorate(Random random, int x, int z) {
        this.decorateScatter(random, x, z, this.range(random, 6, 8), pos -> {
            if (random.nextInt(8) != 0) {
                if (random.nextInt(3) == 0) {
                    TAIGA_1.generate(this.world, random, pos);
                } else {
                    TAIGA_2.generate(this.world, random, pos);
                }
            } else if (random.nextBoolean()) {
                int height = this.range(random, 5, 10);
                new WorldGenTrees(false, height, JUNGLE_LOG, JUNGLE_LEAF, true).generate(this.world, random, pos);
            } else {
                int height = this.range(random, 3, 6);
                if (random.nextInt(10) == 0) {
                    height += 3;
                }
                new WorldGenTrees(false, height, BIRCH_LOG, BIRCH_LEAF, false).generate(this.world, random, pos);
            }
        });
    }
}
