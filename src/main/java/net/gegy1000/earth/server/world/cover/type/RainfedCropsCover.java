package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;

import java.awt.Color;
import java.util.Random;

public class RainfedCropsCover extends EarthCoverType {
    private static final int LAYER_SHORT_GRASS = 0;
    private static final int LAYER_TALL_GRASS = 1;

    public RainfedCropsCover() {
        super(new Color(0xFFFF63));
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
        return CoverBiomeSelectors.BROADLEAF_FOREST_SELECTOR.apply(context.getZone(x, z));
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer grassSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer grass = new SelectWeightedLayer(50,
                    new SelectWeightedLayer.Entry(LAYER_SHORT_GRASS, 10),
                    new SelectWeightedLayer.Entry(LAYER_TALL_GRASS, 5));
            grass = new GenLayerFuzzyZoom(4000, grass);
            grass = new GenLayerFuzzyZoom(5000, grass);

            this.grassSelector = grass;
            this.grassSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
            ShortRaster heightRaster = this.context.getHeightRaster();
            int[] grassLayer = this.sampleChunk(this.grassSelector, chunkPos);

            this.iterateChunk((localX, localZ) -> {
                int y = heightRaster.getShort(localX, localZ);
                switch (grassLayer[localX + localZ * 16]) {
                    case LAYER_SHORT_GRASS:
                        if (random.nextInt(3) == 0) {
                            writer.set(localX, y + 1, localZ, TALL_GRASS);
                        }
                        break;
                    case LAYER_TALL_GRASS:
                        if (random.nextInt(2) == 0) {
                            writer.set(localX, y + 1, localZ, DOUBLE_TALL_GRASS);
                            writer.set(localX, y + 2, localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
                        }
                        break;
                }
            });
        }
    }
}
