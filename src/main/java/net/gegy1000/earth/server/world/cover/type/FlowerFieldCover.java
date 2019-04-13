package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.awt.Color;
import java.util.Random;

public class FlowerFieldCover extends EarthCoverType {
    public FlowerFieldCover() {
        super(new Color(0xAAC700));
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
        return Biomes.BIRCH_FOREST;
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer plantSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer plant = new SelectionSeedLayer(2, 3000);
            plant = new GenLayerVoronoiZoom(1000, plant);
            plant = new GenLayerFuzzyZoom(2000, plant);

            this.plantSelector = plant;
            this.plantSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
            ShortRaster heightRaster = this.context.getHeightRaster();
            int[] plantLayer = this.sampleChunk(this.plantSelector, chunkPos);

            this.iterateChunk((localX, localZ) -> {
                if (random.nextInt(3) == 0) {
                    int index = localX + localZ * 16;
                    int plant = plantLayer[index];
                    int y = heightRaster.getShort(localX, localZ);
                    if (plant == 0) {
                        writer.set(localX, y + 1, localZ, FLOWERS[random.nextInt(FLOWERS.length)]);
                    } else {
                        writer.set(localX, y + 1, localZ, TALL_GRASS);
                    }
                }
            });
        }
    }
}
