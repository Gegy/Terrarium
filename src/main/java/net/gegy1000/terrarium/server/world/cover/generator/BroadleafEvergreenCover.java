package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;

import java.util.Random;

public class BroadleafEvergreenCover extends ForestCover {
    public BroadleafEvergreenCover() {
        super(CoverType.BROADLEAF_EVERGREEN);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(zone == LatitudinalZone.TROPICS ? 1 : 2);

        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);
        int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, x, z);

        int oakCount = this.getOakCount(random, zone);
        this.decorateScatterSample(random, x, z, oakCount, point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 4, 7) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(this.world, random, point.pos);
            }
        });

        this.decorateScatterSample(random, x, z, this.getBirchCount(random, zone), point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 4, 7) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(this.world, random, point.pos);
            }
        });

        int jungleCount = this.getJungleCount(random, zone);
        this.decorateScatterSample(random, x, z, jungleCount, point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 4, 9) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(this.world, random, point.pos);
            }
        });

        this.stopIntersectionPrevention();

        this.decorateScatter(random, x, z, oakCount + 2, point -> {
            OAK_DENSE_SHRUB.generate(this.world, random, point);
        });

        this.decorateScatter(random, x, z, jungleCount + 2, point -> {
            JUNGLE_DENSE_SHRUB.generate(this.world, random, point);
        });
    }

    @Override
    public int getMaxHeightOffset() {
        return 6;
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
                return this.range(random, 1, 4);
            case TROPICS:
                return this.range(random, 3, 7);
            case FRIGID:
                return 0;
            default:
                return this.range(random, -2, 2);
        }
    }
}
