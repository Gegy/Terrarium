package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.tree.GenerousTreeGenerator;

import java.util.Random;

public class OpenNeedleleafCover extends ForestCover {
    public OpenNeedleleafCover() {
        super(CoverType.OPEN_NEEDLELEAF);
    }

    @Override
    public void decorate(Random random, LatitudinalZone zone, int x, int z) {
        this.preventIntersection(1);

        int[] clearingLayer = this.sampleChunk(this.clearingSelector, x, z);
        int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, x, z);

        int spruceCount = this.getSpruceCount(random, zone);
        this.decorateScatterSample(random, x, z, spruceCount, point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                if (random.nextInt(3) == 0) {
                    PINE_TREE.generate(this.world, random, point.pos);
                } else {
                    SPRUCE_TREE.generate(this.world, random, point.pos);
                }
            }
        });

        int birchCount = this.getBirchCount(random, zone);
        this.decorateScatterSample(random, x, z, birchCount, point -> {
            if (clearingLayer[point.chunk.index] == 0) {
                int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, point.chunk);
                boolean vines = random.nextInt(4) == 0;
                new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, vines, false).generate(this.world, random, point.pos);
            }
        });

        this.stopIntersectionPrevention();

        this.decorateScatter(random, x, z, spruceCount, point -> {
            SPRUCE_SMALL_SHRUB.generate(this.world, random, point);
        });

        this.decorateScatter(random, x, z, birchCount, point -> {
            BIRCH_SMALL_SHRUB.generate(this.world, random, point);
        });
    }

    @Override
    public int getMaxHeightOffset() {
        return 6;
    }

    private int getSpruceCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case TEMPERATE:
                return this.range(random, 8, 10);
            default:
                return this.range(random, 6, 8);
        }
    }

    private int getBirchCount(Random random, LatitudinalZone zone) {
        switch (zone) {
            case FRIGID:
                return this.range(random, 4, 6);
            default:
                return this.range(random, 10, 12);
        }
    }
}
