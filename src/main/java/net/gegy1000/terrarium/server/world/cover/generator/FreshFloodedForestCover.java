package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.block.state.IBlockState;

import java.util.Random;

public class FreshFloodedForestCover extends FloodedForestCover {
    public FreshFloodedForestCover() {
        super(CoverType.FRESH_FLOODED_FOREST);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(1);

        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);
        int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, x, z);

        this.decorateScatterSample(random, x, z, this.range(random, 10, 14), point -> {
            int index = point.chunk.index;
            if (clearingLayer[index] == 0) {
                int height = this.range(random, 5, 8) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                if (zone == LatitudinalZone.TROPICS || zone == LatitudinalZone.SUBTROPICS) {
                    if (random.nextInt(3) == 0) {
                        new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, true, false).generate(this.world, random, point.pos);
                    } else {
                        new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(this.world, random, point.pos);
                    }
                } else {
                    if (random.nextInt(3) != 0) {
                        new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, true, false).generate(this.world, random, point.pos);
                    } else {
                        new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, true, false).generate(this.world, random, point.pos);
                    }
                }
            }
        });

        this.stopIntersectionPrevention();
    }

    @Override
    public IBlockState getPrimaryCover() {
        return GRASS;
    }

    @Override
    public boolean hasPodzol() {
        return true;
    }
}
