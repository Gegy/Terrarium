package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.CoverPrimer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class FlowerFieldCover extends EarthCoverType {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;

    @Override
    public EarthSurfaceGenerator createSurfaceGenerator(EarthCoverContext context) {
        return new Surface(context, this);
    }

    @Override
    public CoverDecorationGenerator<EarthCoverContext> createDecorationGenerator(EarthCoverContext context) {
        return new CoverDecorationGenerator.Empty<>(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return Biomes.BIRCH_FOREST;
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer coverSelector;
        private final GenLayer plantSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer cover = new SelectionSeedLayer(2, 1);
            cover = new GenLayerVoronoiZoom(1000, cover);
            cover = new GenLayerFuzzyZoom(3000, cover);

            this.coverSelector = cover;
            this.coverSelector.initWorldGenSeed(context.getSeed());

            GenLayer plant = new SelectionSeedLayer(2, 3000);
            plant = new GenLayerVoronoiZoom(1000, plant);
            plant = new GenLayerFuzzyZoom(2000, plant);

            this.plantSelector = plant;
            this.plantSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.coverFromLayer(coverBlockBuffer, originX, originZ, this.coverSelector, (sampledValue, localX, localZ) -> {
                switch (sampledValue) {
                    case LAYER_GRASS:
                        return GRASS;
                    case LAYER_DIRT:
                    default:
                        return COARSE_DIRT;
                }
            });
        }

        @Override
        public void decorate(int originX, int originZ, CoverPrimer primer, Random random) {
            ShortRasterTile heightRaster = this.context.getHeightRaster();
            int[] plantLayer = this.sampleChunk(this.plantSelector, originX, originZ);

            this.iterateChunk((localX, localZ) -> {
                if (random.nextInt(3) == 0) {
                    int index = localX + localZ * 16;
                    int plant = plantLayer[index];
                    int y = heightRaster.getShort(localX, localZ);
                    if (plant == 0) {
                        primer.setBlockState(localX, y + 1, localZ, FLOWERS[random.nextInt(FLOWERS.length)]);
                    } else {
                        primer.setBlockState(localX, y + 1, localZ, TALL_GRASS);
                    }
                }
            });
        }
    }
}
