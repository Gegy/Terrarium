package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;

import java.util.Random;

public class MixedBroadNeedleleafCover extends ForestCover {
    public MixedBroadNeedleleafCover() {
        super(CoverType.MIXED_BROAD_NEEDLELEAF);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(1);

        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);
        int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, x, z);

        this.decorateScatterSample(random, x, z, this.getSpruceCount(random, zone), point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                if (random.nextInt(3) == 0) {
                    PINE_TREE.generate(this.world, random, point.pos);
                } else {
                    SPRUCE_TREE.generate(this.world, random, point.pos);
                }
            }
        });

        this.decorateScatterSample(random, x, z, this.getOakCount(random, zone), point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(this.world, random, point.pos);
            }
        });

        this.decorateScatterSample(random, x, z, this.getBirchCount(random, zone), point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(this.world, random, point.pos);
            }
        });

        this.decorateScatterSample(random, x, z, this.getJungleCount(random, zone), point -> {
            int index = point.chunk.index;
            if (clearingLayer[index] == 0) {
                int height = this.range(random, 5, 9) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(this.world, random, point.pos);
            }
        });

        this.stopIntersectionPrevention();
    }

    @Override
    public int getMaxHeightOffset() {
        return 5;
    }

    private int getOakCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case SUBTROPICS:
                return this.range(random, 1, 5);
            case TROPICS:
                return this.range(random, 0, 4);
            case FRIGID:
                return this.range(random, 0, 2);
            default:
                return this.range(random, 2, 7);
        }
    }

    private int getBirchCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TEMPERATE:
                return this.range(random, 1, 5);
            case FRIGID:
                return this.range(random, 0, 2);
            default:
                return 0;
        }
    }

    private int getJungleCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case SUBTROPICS:
                return this.range(random, 1, 5);
            case TROPICS:
                return this.range(random, 3, 7);
            case FRIGID:
                return 0;
            default:
                return this.range(random, -2, 2);
        }
    }

    private int getSpruceCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TEMPERATE:
                return this.range(random, 4, 6);
            default:
                return this.range(random, 2, 4);
        }
    }
}
