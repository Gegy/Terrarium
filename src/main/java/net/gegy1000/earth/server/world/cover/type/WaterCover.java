package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.IntCache;

import java.util.Random;

public class WaterCover extends EarthCoverType {
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
        return Biomes.OCEAN;
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer coverSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer layer = new SelectionSeedLayer(2, 1);
            layer = new GenLayerFuzzyZoom(1000, layer);
            layer = new CoverLayer(2000, layer);
            layer = new GenLayerVoronoiZoom(3000, layer);
            layer = new GenLayerFuzzyZoom(4000, layer);

            this.coverSelector = layer;
            this.coverSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.populateBlockFiller(random, originX, originZ, coverBlockBuffer);
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            this.coverFromLayer(fillerBlockBuffer, originX, originZ, this.coverSelector, (sampledValue, localX, localZ) -> {
                switch (sampledValue) {
                    case 0:
                        return SAND;
                    case 1:
                        return GRAVEL;
                    case 2:
                        return COARSE_DIRT;
                    case 3:
                        return CLAY;
                    default:
                        return COARSE_DIRT;
                }
            });
        }
    }

    private static class CoverLayer extends GenLayer {
        private CoverLayer(long seed, GenLayer parent) {
            super(seed);
            this.parent = parent;
        }

        @Override
        public int[] getInts(int areaX, int areaY, int areaWidth, int areaHeight) {
            int[] parent = this.parent.getInts(areaX, areaY, areaWidth, areaHeight);
            int[] result = IntCache.getIntCache(areaWidth * areaHeight);
            for (int z = 0; z < areaHeight; z++) {
                for (int x = 0; x < areaWidth; x++) {
                    this.initChunkSeed(areaX + x, areaY + z);
                    int index = x + z * areaWidth;
                    int sample = parent[index];
                    if (sample == 0) {
                        result[index] = this.nextInt(2);
                    } else {
                        result[index] = this.nextInt(20) == 0 ? 3 : 2;
                    }
                }
            }
            return result;
        }
    }
}
