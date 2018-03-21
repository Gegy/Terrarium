package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.CoverPrimer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class BareCover implements CoverType {
    private static final int LAYER_DIRT = 0;
    private static final int LAYER_GRAVEL = 1;
    private static final int LAYER_SAND = 2;

    @Override
    public CoverSurfaceGenerator createSurfaceGenerator(CoverGenerationContext context) {
        return new Surface(context, this);
    }

    @Override
    public CoverDecorationGenerator createDecorationGenerator(CoverGenerationContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(int x, int z) {
        return Biomes.DESERT;
    }

    private static class Surface extends EarthSurfaceGenerator {
        private static final BlockProvider BLOCK_PROVIDER = (sampledValue, slope) -> {
            switch (sampledValue) {
                case LAYER_GRAVEL:
                    return slope >= MOUNTAINOUS_SLOPE ? COBBLESTONE : GRAVEL;
                case LAYER_SAND:
                    return slope >= MOUNTAINOUS_SLOPE ? SANDSTONE : SAND;
                default:
                    return slope >= MOUNTAINOUS_SLOPE ? HARDENED_CLAY : SAND;
            }
        };

        private final GenLayer coverSelector;

        private Surface(CoverGenerationContext context, CoverType coverType) {
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
            this.coverFromLayer(coverBlockBuffer, originX, originZ, this.coverSelector, BLOCK_PROVIDER);
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            this.coverFromLayer(fillerBlockBuffer, originX, originZ, this.coverSelector, BLOCK_PROVIDER);
        }

        @Override
        public void decorate(int originX, int originZ, CoverPrimer primer, Random random) {
            ShortRasterTileAccess heightRaster = this.context.getHeightRaster();
            ByteRasterTileAccess slopeRaster = this.context.getSlopeRaster();
            this.iterateChunk((localX, localZ) -> {
                int slope = slopeRaster.getUnsigned(localX, localZ);
                if (slope < MOUNTAINOUS_SLOPE && random.nextInt(250) == 0) {
                    int y = heightRaster.getShort(localX, localZ);
                    primer.setBlockState(localX, y + 1, localZ, DEAD_BUSH);
                }
            });
        }
    }

    private static class Decoration extends CoverDecorationGenerator {
        private Decoration(CoverGenerationContext context, CoverType coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(int originX, int originZ, Random random) {
            World world = this.context.getWorld();
            ByteRasterTileAccess slopeRaster = this.context.getSlopeRaster();

            this.preventIntersection(5);

            this.decorateScatter(random, originX, originZ, this.range(random, -16, 1), (pos, localX, localZ) -> {
                if (slopeRaster.getUnsigned(localX, localZ) < MOUNTAINOUS_SLOPE) {
                    OAK_TALL_SHRUB.generate(world, random, pos);
                }
            });

            this.stopIntersectionPrevention();
        }
    }
}
