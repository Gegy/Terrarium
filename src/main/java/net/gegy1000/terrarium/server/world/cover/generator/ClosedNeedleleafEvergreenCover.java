package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;

import java.util.Random;

public class ClosedNeedleleafEvergreenCover extends ClosedForestCover {
    public ClosedNeedleleafEvergreenCover() {
        super(CoverType.CLOSED_NEEDLELEAF_EVERGREEN);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(1);

        int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, x, z);

        this.decorateScatter(random, x, z, this.getSpruceCount(random, zone), pos -> {
            if (random.nextInt(3) == 0) {
                PINE_TREE.generate(this.world, random, pos);
            } else {
                SPRUCE_TREE.generate(this.world, random, pos);
            }
        });

        this.decorateScatterSample(random, x, z, this.getBirchCount(random, zone), point -> {
            int height = this.range(random, 5, 6) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
            new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, true, false).generate(this.world, random, point.pos);
        });

        this.stopIntersectionPrevention();
    }

    @Override
    public int getMaxHeightOffset() {
        return 6;
    }

    private int getSpruceCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TEMPERATE:
                return this.range(random, 7, 9);
            default:
                return this.range(random, 6, 7);
        }
    }

    private int getBirchCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case FRIGID:
                return this.range(random, 4, 6);
            default:
                return this.range(random, 6, 9);
        }
    }
}
