package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public abstract class ForestCover extends EarthCoverType {
    private static final int HEIGHT_STEP = 2;

    protected static final int LAYER_PRIMARY = 0;
    protected static final int LAYER_DIRT = 1;
    protected static final int LAYER_PODZOL = 2;

    @Override
    public EarthSurfaceGenerator createSurfaceGenerator(EarthCoverContext context) {
        return new Surface(context, this);
    }

    protected static abstract class Decoration extends EarthDecorationGenerator {
        protected final GenLayer clearingSelector;
        protected final GenLayer heightOffsetSelector;

        protected Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer clearing = new SelectWeightedLayer(2,
                    new SelectWeightedLayer.Entry(0, 6),
                    new SelectWeightedLayer.Entry(1, 4));
            clearing = new GenLayerVoronoiZoom(4000, clearing);
            clearing = new ReplaceRandomLayer(0, 2, 16, 6000, clearing);
            clearing = new ReplaceRandomLayer(1, 0, 10, 7000, clearing);
            clearing = new GenLayerFuzzyZoom(5000, clearing);
            clearing = new GenLayerVoronoiZoom(8000, clearing);
            clearing = new GenLayerFuzzyZoom(9000, clearing);

            this.clearingSelector = clearing;

            this.clearingSelector.initWorldGenSeed(context.getSeed());

            GenLayer heightOffset = new SelectionSeedLayer((this.getMaxHeightOffset() / HEIGHT_STEP) + 1, 3);
            heightOffset = new GenLayerVoronoiZoom(10000, heightOffset);
            heightOffset = new GenLayerFuzzyZoom(11000, heightOffset);
            heightOffset = new GenLayerVoronoiZoom(12000, heightOffset);
            heightOffset = new GenLayerFuzzyZoom(13000, heightOffset);
            heightOffset = new GenLayerVoronoiZoom(14000, heightOffset);
            heightOffset = new GenLayerFuzzyZoom(15000, heightOffset);

            this.heightOffsetSelector = heightOffset;
            this.heightOffsetSelector.initWorldGenSeed(context.getSeed());
        }

        protected int sampleHeightOffset(int[] heightOffsetLayer, int localX, int localZ) {
            return heightOffsetLayer[localX + localZ * 16] * HEIGHT_STEP;
        }

        public int getMaxHeightOffset() {
            return 6;
        }
    }

    protected static class Surface extends EarthSurfaceGenerator {
        protected final GenLayer coverSelector;

        protected Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            this.coverSelector = this.createCoverSelector();
            this.coverSelector.initWorldGenSeed(this.context.getSeed());
        }

        protected GenLayer createCoverSelector() {
            GenLayer cover = new SelectionSeedLayer(2, 1);
            cover = new GenLayerVoronoiZoom(1000, cover);
            cover = new ReplaceRandomLayer(ForestCover.LAYER_DIRT, ForestCover.LAYER_PODZOL, 4, 2000, cover);
            cover = new GenLayerFuzzyZoom(3000, cover);
            return cover;
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.coverFromLayer(coverBlockBuffer, originX, originZ, this.coverSelector, (sampledValue, localX, localZ) -> {
                switch (sampledValue) {
                    case LAYER_PRIMARY:
                        return GRASS;
                    case LAYER_DIRT:
                        return COARSE_DIRT;
                    default:
                        return PODZOL;
                }
            });
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
            ShortRasterTile heightRaster = this.context.getHeightRaster();

            this.iterateChunk((localX, localZ) -> {
                if (random.nextInt(4) == 0) {
                    int y = heightRaster.getShort(localX, localZ);
                    IBlockState state = writer.get(localX, y, localZ);
                    if (state.getMaterial() == Material.WATER) {
                        if (random.nextInt(16) == 0) {
                            writer.set(localX, y + 1, localZ, LILYPAD);
                        }
                    } else {
                        writer.set(localX, y + 1, localZ, TALL_GRASS);
                    }
                }
            });
        }
    }
}
