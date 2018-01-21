package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.tree.GenerousTreeGenerator;

import java.util.Random;

public class OpenBroadleafDeciduousCover extends ForestCover {
    public OpenBroadleafDeciduousCover() {
        super(CoverType.OPEN_BROADLEAF_DECIDUOUS);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(1);

        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);
        int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, x, z);

        int oakCount = this.getOakCount(random, zone);
        this.decorateScatterSample(random, x, z, oakCount, point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 4, 6) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(this.world, random, point.pos);
            }
        });

        int birchCount = this.getBirchCount(random, zone);
        this.decorateScatterSample(random, x, z, birchCount, point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 4, 6) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(this.world, random, point.pos);
            }
        });

        int jungleCount = this.getJungleCount(random, zone);
        this.decorateScatterSample(random, x, z, jungleCount, point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 4, 8) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, false, false).generate(this.world, random, point.pos);
            }
        });

        this.stopIntersectionPrevention();

        this.decorateScatter(random, x, z, oakCount, point -> {
            OAK_SMALL_SHRUB.generate(this.world, random, point);
        });

        this.decorateScatter(random, x, z, birchCount, point -> {
            BIRCH_SMALL_SHRUB.generate(this.world, random, point);
        });

        this.decorateScatter(random, x, z, jungleCount, point -> {
            JUNGLE_SMALL_SHRUB.generate(this.world, random, point);
        });
    }

    @Override
    public int getMaxHeightOffset() {
        return 4;
    }

    private int getOakCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case SUBTROPICS:
                return this.range(random, 1, 3);
            case TROPICS:
                return this.range(random, 0, 2);
            case FRIGID:
                return this.range(random, 0, 1);
            default:
                return this.range(random, 2, 5);
        }
    }

    private int getBirchCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TEMPERATE:
                return this.range(random, 1, 3);
            case FRIGID:
                return this.range(random, 0, 1);
            default:
                return 0;
        }
    }

    private int getJungleCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case SUBTROPICS:
                return this.range(random, 1, 3);
            case TROPICS:
                return this.range(random, 3, 5);
            case FRIGID:
                return 0;
            default:
                return this.range(random, -2, 1);
        }
    }
}
