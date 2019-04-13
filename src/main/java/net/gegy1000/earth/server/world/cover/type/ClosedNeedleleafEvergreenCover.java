package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.ClimaticZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.awt.Color;
import java.util.Random;

public class ClosedNeedleleafEvergreenCover extends ClosedForestCover {
    public ClosedNeedleleafEvergreenCover() {
        super(new Color(0x003B00));
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return CoverBiomeSelectors.NEEDLELEAF_FOREST_SELECTOR.apply(context.getZone(x, z));
    }

    private static class Decoration extends ClosedForestCover.Decoration {
        private Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPopulationWriter writer, Random random) {
            World world = this.context.getWorld();
            ClimaticZone zone = this.context.getZone(chunkPos);

            this.preventIntersection(1);

            int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, chunkPos);

            this.decorateScatter(random, chunkPos, writer, this.getSpruceCount(random, zone), (pos, localX, localZ) -> {
                if (random.nextInt(3) == 0) {
                    PINE_TREE.generate(world, random, pos);
                } else {
                    SPRUCE_TREE.generate(world, random, pos);
                }
            });

            this.decorateScatter(random, chunkPos, writer, this.getBirchCount(random, zone), (pos, localX, localZ) -> {
                int height = this.range(random, 5, 6) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, true, false).generate(world, random, pos);
            });

            this.stopIntersectionPrevention();
        }

        private int getSpruceCount(Random random, ClimaticZone zone) {
            switch (zone) {
                case TEMPERATE:
                    return this.range(random, 7, 9);
                default:
                    return this.range(random, 6, 7);
            }
        }

        private int getBirchCount(Random random, ClimaticZone zone) {
            switch (zone) {
                case FRIGID:
                    return this.range(random, 4, 6);
                default:
                    return this.range(random, 6, 9);
            }
        }
    }
}
