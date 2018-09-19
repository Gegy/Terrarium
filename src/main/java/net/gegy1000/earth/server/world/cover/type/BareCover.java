package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.chunk.ComposeChunk;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.UnsignedByteRasterTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class BareCover extends EarthCoverType {
    private static final int LAYER_DIRT = 0;
    private static final int LAYER_GRAVEL = 1;
    private static final int LAYER_SAND = 2;

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return Biomes.DESERT;
    }

    @Override
    public EarthSurfaceGenerator createSurfaceGenerator(EarthCoverContext context) {
        return new Surface(context, this);
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer coverSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer layer = new SelectWeightedLayer(1,
                    new SelectWeightedLayer.Entry(LAYER_GRAVEL, 2),
                    new SelectWeightedLayer.Entry(LAYER_DIRT, 10),
                    new SelectWeightedLayer.Entry(LAYER_SAND, 5));
            layer = new GenLayerVoronoiZoom(1000, layer);
            layer = new GenLayerFuzzyZoom(2000, layer);

            this.coverSelector = layer;
            this.coverSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            Provider blockProvider = new Provider(this.context.getSlopeRaster());
            this.coverFromLayer(coverBlockBuffer, originX, originZ, this.coverSelector, blockProvider);
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            Provider blockProvider = new Provider(this.context.getSlopeRaster());
            this.coverFromLayer(fillerBlockBuffer, originX, originZ, this.coverSelector, blockProvider);
        }

        @Override
        public void decorate(int originX, int originZ, ComposeChunk chunk, Random random) {
            ShortRasterTile heightRaster = this.context.getHeightRaster();
            UnsignedByteRasterTile slopeRaster = this.context.getSlopeRaster();
            this.iterateChunk((localX, localZ) -> {
                int slope = slopeRaster.getByte(localX, localZ);
                if (slope < MOUNTAINOUS_SLOPE && random.nextInt(250) == 0) {
                    int y = heightRaster.getShort(localX, localZ);
                    chunk.set(localX, y + 1, localZ, DEAD_BUSH);
                }
            });
        }

        private class Provider implements BlockProvider {
            private final UnsignedByteRasterTile slopeRaster;

            private Provider(UnsignedByteRasterTile slopeRaster) {
                this.slopeRaster = slopeRaster;
            }

            @Override
            public IBlockState provideBlock(int sampledValue, int localX, int localZ) {
                int slope = this.slopeRaster.getByte(localX, localZ);
                switch (sampledValue) {
                    case LAYER_GRAVEL:
                        return slope >= MOUNTAINOUS_SLOPE ? COBBLESTONE : GRAVEL;
                    case LAYER_SAND:
                        return slope >= MOUNTAINOUS_SLOPE ? SANDSTONE : SAND;
                    default:
                        return slope >= MOUNTAINOUS_SLOPE ? HARDENED_CLAY : SAND;
                }
            }
        }
    }

    private static class Decoration extends EarthDecorationGenerator {
        private Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(int originX, int originZ, Random random) {
            World world = this.context.getWorld();
            UnsignedByteRasterTile slopeRaster = this.context.getSlopeRaster();

            this.preventIntersection(5);

            this.decorateScatter(random, originX, originZ, this.range(random, -16, 1), (pos, localX, localZ) -> {
                if (slopeRaster.getByte(localX, localZ) < MOUNTAINOUS_SLOPE) {
                    OAK_TALL_SHRUB.generate(world, random, pos);
                }
            });

            this.stopIntersectionPrevention();
        }
    }
}
