package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.CoverPrimer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class RainfedCropsCover implements CoverType {
    protected static final int LAYER_GRASS = 0;
    protected static final int LAYER_PODZOL = 1;

    private static final int LAYER_SHORT_GRASS = 0;
    private static final int LAYER_TALL_GRASS = 1;

    @Override
    public CoverSurfaceGenerator createSurfaceGenerator(CoverGenerationContext context) {
        return new Surface(context, this);
    }

    @Override
    public CoverDecorationGenerator createDecorationGenerator(CoverGenerationContext context) {
        return new CoverDecorationGenerator.Empty(context, this);
    }

    @Override
    public Biome getBiome(int x, int z) {
        // TODO
        return CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR.apply(LatitudinalZone.TROPICS);
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer coverSelector;
        private final GenLayer grassSelector;

        private Surface(CoverGenerationContext context, CoverType coverType) {
            super(context, coverType);

            GenLayer cover = new SelectWeightedLayer(10,
                    new SelectWeightedLayer.Entry(LAYER_GRASS, 10),
                    new SelectWeightedLayer.Entry(LAYER_PODZOL, 4));
            cover = new GenLayerVoronoiZoom(2000, cover);
            cover = new GenLayerFuzzyZoom(3000, cover);

            this.coverSelector = cover;
            this.coverSelector.initWorldGenSeed(context.getSeed());

            GenLayer grass = new SelectWeightedLayer(50,
                    new SelectWeightedLayer.Entry(LAYER_SHORT_GRASS, 10),
                    new SelectWeightedLayer.Entry(LAYER_TALL_GRASS, 5));
            grass = new GenLayerFuzzyZoom(4000, grass);
            grass = new GenLayerFuzzyZoom(5000, grass);

            this.grassSelector = grass;
            this.grassSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.coverFromLayer(coverBlockBuffer, originX, originZ, this.coverSelector, (sampledValue, slope) -> {
                switch (sampledValue) {
                    case LAYER_GRASS:
                        return GRASS;
                    default:
                        return PODZOL;
                }
            });
        }

        @Override
        public void decorate(int originX, int originZ, CoverPrimer primer, Random random) {
            ShortRasterTileAccess heightRaster = this.context.getHeightRaster();
            int[] grassLayer = this.sampleChunk(this.grassSelector, originX, originZ);

            this.iterateChunk((localX, localZ) -> {
                int y = heightRaster.getShort(localX, localZ);
                switch (grassLayer[localX + localZ * 16]) {
                    case LAYER_SHORT_GRASS:
                        if (random.nextInt(3) == 0) {
                            primer.setBlockState(localX, y + 1, localZ, TALL_GRASS);
                        }
                        break;
                    case LAYER_TALL_GRASS:
                        if (random.nextInt(2) == 0) {
                            primer.setBlockState(localX, y + 1, localZ, DOUBLE_TALL_GRASS);
                            primer.setBlockState(localX, y + 2, localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
                        }
                        break;
                }
            });
        }
    }
}
