package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.awt.Color;
import java.util.Random;

public class ScreeCover extends EarthCoverType implements BeachyCover {
    private static final int LAYER_STONE = 0;
    private static final int LAYER_GRAVEL = 1;
    private static final int LAYER_DIRT = 2;

    public ScreeCover() {
        super(new Color(0xAFAFAF));
    }

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
        return Biomes.EXTREME_HILLS;
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer coverSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer cover = new SelectionSeedLayer(2, 1);
            cover = new GenLayerVoronoiZoom(1000, cover);
            cover = new ReplaceRandomLayer(LAYER_GRAVEL, LAYER_DIRT, 2, 2000, cover);
            cover = new GenLayerFuzzyZoom(3000, cover);

            this.coverSelector = cover;
            this.coverSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.coverFromLayer(coverBlockBuffer, originX, originZ, this.coverSelector, (sampledValue, localX, localZ) -> {
                switch (sampledValue) {
                    case LAYER_STONE:
                        return COBBLESTONE;
                    case LAYER_DIRT:
                        return COARSE_DIRT;
                    case LAYER_GRAVEL:
                    default:
                        return GRAVEL;
                }
            });
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            this.coverBlock(fillerBlockBuffer, COBBLESTONE);
        }
    }
}
