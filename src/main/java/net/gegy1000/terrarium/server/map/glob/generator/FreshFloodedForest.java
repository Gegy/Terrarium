package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.feature.WorldGenTrees;

import java.util.Random;

public class FreshFloodedForest extends Forest {
    public FreshFloodedForest() {
        super(GlobType.FRESH_FLOODED_FOREST);
    }

    @Override
    public void decorate(Random random, int x, int z) {
        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);

        this.decorateScatter(random, clearingLayer, x, z, this.range(random, 5, 7), pos -> {
            int height = this.range(random, 3, 6);
            if (random.nextInt(10) == 0) {
                height += 3;
            }
            if (random.nextBoolean()) {
                new WorldGenTrees(false, height, OAK_LOG, OAK_LEAF, true).generate(this.world, random, pos);
            } else {
                new WorldGenTrees(false, height, BIRCH_LOG, BIRCH_LEAF, true).generate(this.world, random, pos);
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
