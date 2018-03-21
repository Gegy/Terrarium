package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class BroadleafEvergreenCover extends ForestCover {
    @Override
    public CoverDecorationGenerator createDecorationGenerator(CoverGenerationContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(int x, int z) {
        // TODO: Latitudinal zone
        return CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR.apply(LatitudinalZone.TROPICS);
    }

    private static class Decoration extends ForestCover.Decoration {
        private Decoration(CoverGenerationContext context, CoverType coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(int originX, int originZ, Random random) {
            World world = this.context.getWorld();
            LatitudinalZone zone = LatitudinalZone.TROPICS;

            this.preventIntersection(zone == LatitudinalZone.TROPICS ? 1 : 2);

            int[] clearingLayer = this.sampleChunk(this.clearingSelector, originX, originZ);
            int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, originX, originZ);

            int oakCount = this.getOakCount(random, zone);
            this.decorateScatter(random, originX, originZ, oakCount, (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 4, 7) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(world, random, pos);
                }
            });

            this.decorateScatter(random, originX, originZ, this.getBirchCount(random, zone), (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 4, 7) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(world, random, pos);
                }
            });

            int jungleCount = this.getJungleCount(random, zone);
            this.decorateScatter(random, originX, originZ, jungleCount, (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 4, 9) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(world, random, pos);
                }
            });

            this.stopIntersectionPrevention();

            this.decorateScatter(random, originX, originZ, oakCount + 2, (pos, localX, localZ) -> {
                OAK_DENSE_SHRUB.generate(world, random, pos);
            });

            this.decorateScatter(random, originX, originZ, jungleCount + 2, (pos, localX, localZ) -> {
                JUNGLE_DENSE_SHRUB.generate(world, random, pos);
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
}