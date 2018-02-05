package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.tree.GenerousTreeGenerator;

import java.util.Random;

public class ClosedBroadleafDeciduousCover extends ClosedForestCover {
    public ClosedBroadleafDeciduousCover() {
        super(CoverType.CLOSED_BROADLEAF_DECIDUOUS);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(zone == LatitudinalZone.TROPICS ? 1 : 2);

        int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, x, z);

        int oakCount = this.getOakCount(random, zone);
        this.decorateScatterSample(random, x, z, oakCount, point -> {
            int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
            new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(this.world, random, point.pos);
        });

        this.decorateScatterSample(random, x, z, this.getBirchCount(random, zone), point -> {
            int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
            new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(this.world, random, point.pos);
        });

        int jungleCount = this.getJungleCount(random, zone);
        this.decorateScatterSample(random, x, z, jungleCount, point -> {
            int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
            new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, false, false).generate(this.world, random, point.pos);
        });

        this.stopIntersectionPrevention();

        this.decorateScatter(random, x, z, oakCount + 4, point -> {
            OAK_DENSE_SHRUB.generate(this.world, random, point);
        });

        this.decorateScatter(random, x, z, jungleCount + 4, point -> {
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
                return this.range(random, 5, 7);
            case TROPICS:
                return this.range(random, 4, 6);
            case FRIGID:
                return this.range(random, 3, 5);
            default:
                return this.range(random, 6, 9);
        }
    }

    private int getBirchCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TEMPERATE:
                return this.range(random, 4, 7);
            case FRIGID:
                return this.range(random, 2, 4);
            default:
                return 0;
        }
    }

    private int getJungleCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case SUBTROPICS:
                return this.range(random, 5, 7);
            case TROPICS:
                return this.range(random, 6, 9);
            case FRIGID:
                return 0;
            default:
                return this.range(random, -2, 3);
        }
    }
}
