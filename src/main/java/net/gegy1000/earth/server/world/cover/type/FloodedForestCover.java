package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

import java.util.Random;

public abstract class FloodedForestCover extends ForestCover {
    @Override
    public abstract Surface createSurfaceGenerator(EarthCoverContext context);

    protected static class Surface extends ForestCover.Surface {
        private final IBlockState primaryCover;
        private final boolean addPodzol;

        protected final GenLayer waterSelector;

        protected Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType, IBlockState primaryCover, boolean addPodzol) {
            super(context, coverType);

            this.primaryCover = primaryCover;
            this.addPodzol = addPodzol;

            GenLayer water = new SelectionSeedLayer(2, 2);
            water = new GenLayerFuzzyZoom(11000, water);
            water = new GenLayerVoronoiZoom(12000, water);
            water = new OutlineEdgeLayer(3, 13000, water);
            water = new GenLayerZoom(14000, water);

            this.waterSelector = water;
            this.waterSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        protected GenLayer createCoverSelector() {
            GenLayer cover = new SelectionSeedLayer(2, 1);
            if (this.addPodzol) {
                cover = new ReplaceRandomLayer(LAYER_PRIMARY, LAYER_PODZOL, 2, 6000, cover);
            }
            cover = new GenLayerVoronoiZoom(7000, cover);
            if (this.addPodzol) {
                cover = new ReplaceRandomLayer(LAYER_PODZOL, LAYER_DIRT, 3, 8000, cover);
            }
            cover = new GenLayerFuzzyZoom(9000, cover);
            return cover;
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            int[] cover = this.sampleChunk(this.coverSelector, originX, originZ);
            int[] water = this.sampleChunk(this.waterSelector, originX, originZ);
            this.iterateChunk((localX, localZ) -> {
                int index = localX + localZ * 16;
                if (water[index] == 3) {
                    coverBlockBuffer[index] = WATER;
                } else {
                    switch (cover[index]) {
                        case LAYER_PRIMARY:
                            coverBlockBuffer[index] = this.primaryCover;
                            break;
                        case LAYER_PODZOL:
                            coverBlockBuffer[index] = PODZOL;
                            break;
                        default:
                            coverBlockBuffer[index] = COARSE_DIRT;
                            break;
                    }
                }
            });
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            this.coverBlock(fillerBlockBuffer, COARSE_DIRT);
        }
    }
}
