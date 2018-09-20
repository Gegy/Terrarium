package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class OpenNeedleleafCover extends ForestCover {
    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return CoverBiomeSelectors.NEEDLELEAF_FOREST_SELECTOR.apply(context.getZone(x, z));
    }

    private static class Decoration extends ForestCover.Decoration {
        private Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPopulationWriter writer, Random random) {
            World world = this.context.getWorld();
            LatitudinalZone zone = this.context.getZone(chunkPos);

            this.preventIntersection(1);

            int[] clearingLayer = this.sampleChunk(this.clearingSelector, chunkPos);
            int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, chunkPos);

            int spruceCount = this.getSpruceCount(random, zone);
            this.decorateScatter(random, chunkPos, writer, spruceCount, (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    if (random.nextInt(3) == 0) {
                        PINE_TREE.generate(world, random, pos);
                    } else {
                        SPRUCE_TREE.generate(world, random, pos);
                    }
                }
            });

            int birchCount = this.getBirchCount(random, zone);
            this.decorateScatter(random, chunkPos, writer, birchCount, (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 5, 7) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    boolean vines = random.nextInt(4) == 0;
                    new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, vines, false).generate(world, random, pos);
                }
            });

            this.stopIntersectionPrevention();

            this.decorateScatter(random, chunkPos, writer, spruceCount, (pos, localX, localZ) -> SPRUCE_SMALL_SHRUB.generate(world, random, pos));

            this.decorateScatter(random, chunkPos, writer, birchCount, (pos, localX, localZ) -> BIRCH_SMALL_SHRUB.generate(world, random, pos));
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
}
