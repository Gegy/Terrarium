package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.Random;

public class SalineFloodedForest extends Forest {
    public SalineFloodedForest() {
        super(GlobType.SALINE_FLOODED_FOREST);
    }

    @Override
    public void decorate(Random random, int x, int z) {
        this.decorateScatter(random, x, z, this.range(random, 4, 8), pos -> {
            if (random.nextInt(5) == 0) {
                int height = this.range(random, 3, 6);
                if (random.nextInt(10) == 0) {
                    height += 3;
                }
                new WorldGenTrees(false, height, Forest.OAK_LOG, Forest.OAK_LEAF, true).generate(this.world, random, pos);
            } else {
                int height = this.range(random, 6, 12);
                new WorldGenTrees(false, height, Forest.JUNGLE_LOG, Forest.JUNGLE_LEAF, true).generate(this.world, random, pos);
            }
        });
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector, type -> {
            switch (type) {
                case LAYER_GRASS:
                    return random.nextInt(3) != 0 ? WATER : PODZOL;
                case LAYER_DIRT:
                    return PODZOL;
                default:
                    return COARSE_DIRT;
            }
        });
    }

    @Override
    protected IBlockState getFillerAt(Random random, int x, int z) {
        return COARSE_DIRT;
    }
}
