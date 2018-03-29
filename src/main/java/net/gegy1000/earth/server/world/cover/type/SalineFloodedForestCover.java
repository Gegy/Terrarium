package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.primer.CoverPrimer;
import net.gegy1000.terrarium.server.world.feature.tree.GenerousTreeGenerator;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Random;

public class SalineFloodedForestCover extends FloodedForestCover {
    @Override
    public Surface createSurfaceGenerator(EarthCoverContext context) {
        return new Surface(context, this);
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(int x, int z) {
        // TODO
        return CoverBiomeSelectors.SALINE_FLOODED_SELECTOR.apply(LatitudinalZone.TROPICS);
    }

    private static class Surface extends FloodedForestCover.Surface {
        private Surface(EarthCoverContext context, CoverType coverType) {
            super(context, coverType, SAND, false);
        }

        @Override
        public void decorate(int originX, int originZ, CoverPrimer primer, Random random) {
            ShortRasterTileAccess heightRaster = this.context.getHeightRaster();

            this.iterateChunk((localX, localZ) -> {
                if (random.nextInt(3) == 0) {
                    int y = heightRaster.getShort(localX, localZ);
                    IBlockState state = primer.getBlockState(localX, y, localZ);
                    if (state.getMaterial() == Material.GROUND) {
                        primer.setBlockState(localX, y + 1, localZ, TALL_GRASS);
                    }
                }
            });
        }
    }

    private static class Decoration extends ForestCover.Decoration {
        private Decoration(EarthCoverContext context, CoverType coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(int originX, int originZ, Random random) {
            World world = this.context.getWorld();

            this.preventIntersection(1);

            int[] clearingLayer = this.sampleChunk(this.clearingSelector, originX, originZ);
            int[] heightOffsetLayer = this.sampleChunk(this.heightOffsetSelector, originX, originZ);

            this.decorateScatter(random, originX, originZ, this.range(random, 8, 10), (pos, localX, localZ) -> {
                if (clearingLayer[localX + localZ * 16] == 0) {
                    int height = this.range(random, 5, 8) + this.sampleHeightOffset(heightOffsetLayer, localX, localZ);
                    BlockPos ground = pos.down();
                    if (world.getBlockState(ground).getMaterial() == Material.SAND) {
                        world.setBlockState(ground, COARSE_DIRT);
                    }
                    if (random.nextInt(4) == 0) {
                        new GenerousTreeGenerator(false, height, OAK_LOG, OAK_LEAF, false, false).generate(world, random, pos);
                    } else {
                        new GenerousTreeGenerator(false, height, BIRCH_LOG, BIRCH_LEAF, false, false).generate(world, random, pos);
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
