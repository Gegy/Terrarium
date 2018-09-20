package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class FreshFloodedForestCover extends FloodedForestCover {
    @Override
    public Surface createSurfaceGenerator(EarthCoverContext context) {
        return new Surface(context, this, Blocks.GRASS.getDefaultState(), true);
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return CoverBiomeSelectors.FLOODED_SELECTOR.apply(context.getZone(x, z));
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

            this.decorateScatter(random, chunkPos, writer, this.range(random, 10, 14), (pos, localX, localZ) -> {
                int index = localX + localZ * 16;
                if (clearingLayer[index] == 0) {
                    int height = this.range(random, 5, 8) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    if (zone == LatitudinalZone.TROPICS || zone == LatitudinalZone.SUBTROPICS) {
                        if (random.nextInt(3) == 0) {
                            new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, true, false).generate(world, random, pos);
                        } else {
                            new GenerousTreeGenerator(false, height, JUNGLE_LOG, JUNGLE_LEAF, true, true).generate(world, random, pos);
                        }
                    } else {
                        if (random.nextInt(3) != 0) {
                            new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, true, false).generate(world, random, pos);
                        } else {
                            new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, true, false).generate(world, random, pos);
                        }
                    }
                }
            });

            this.stopIntersectionPrevention();
        }

        @Override
        public int getMaxHeightOffset() {
            return 5;
        }
    }
}
