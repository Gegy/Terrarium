package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class MixedBroadNeedleleafCover extends ForestCover {
    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return Biomes.FOREST;
    }

    private static class Decoration extends ForestCover.Decoration {
        private Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(int originX, int originZ, Random random) {
            World world = this.context.getWorld();
            LatitudinalZone zone = this.context.getZone(originX, originZ);

            this.preventIntersection(1);

            int[] clearingLayer = this.sampleChunk(this.clearingSelector, originX, originZ);
            int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, originX, originZ);

            this.decorateScatter(random, originX, originZ, this.getSpruceCount(random, zone), (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    if (random.nextInt(3) == 0) {
                        PINE_TREE.generate(world, random, pos);
                    } else {
                        SPRUCE_TREE.generate(world, random, pos);
                    }
                }
            });

            this.decorateScatter(random, originX, originZ, this.getOakCount(random, zone), (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(world, random, pos);
                }
            });

            this.decorateScatter(random, originX, originZ, this.getBirchCount(random, zone), (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(world, random, pos);
                }
            });

            this.decorateScatter(random, originX, originZ, this.getJungleCount(random, zone), (pos, localX, localZ) -> {
                int index = localX + localZ * 16;
                if (clearingLayer[index] == 0) {
                    int height = this.range(random, 5, 9) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(world, random, pos);
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
}
