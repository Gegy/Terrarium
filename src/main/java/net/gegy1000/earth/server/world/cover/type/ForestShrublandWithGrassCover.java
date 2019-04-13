package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.ClimaticZone;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.awt.Color;
import java.util.Random;

public class ForestShrublandWithGrassCover extends EarthCoverType {
    public ForestShrublandWithGrassCover() {
        super(new Color(0x8D9F00));
    }

    @Override
    public EarthSurfaceGenerator createSurfaceGenerator(EarthCoverContext context) {
        return new Surface(context, this);
    }

    @Override
    public EarthDecorationGenerator createDecorationGenerator(EarthCoverContext context) {
        return new Decoration(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return CoverBiomeSelectors.FOREST_SHRUBLAND_SELECTOR.apply(context.getZone(x, z));
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer grassSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer grass = new SelectionSeedLayer(2, 3000);
            grass = new GenLayerVoronoiZoom(1000, grass);
            grass = new GenLayerFuzzyZoom(2000, grass);

            this.grassSelector = grass;
            this.grassSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
            ShortRaster heightRaster = this.context.getHeightRaster();
            int[] grassLayer = this.sampleChunk(this.grassSelector, chunkPos);

            this.iterateChunk((localX, localZ) -> {
                int y = heightRaster.getShort(localX, localZ);
                if (grassLayer[localX + localZ * 16] == 1 && random.nextInt(4) != 0) {
                    writer.set(localX, y + 1, localZ, TALL_GRASS);
                } else if (random.nextInt(8) == 0) {
                    writer.set(localX, y + 1, localZ, BUSH);
                }
            });
        }
    }

    private static class Decoration extends EarthDecorationGenerator {
        private Decoration(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPopulationWriter writer, Random random) {
            World world = this.context.getWorld();
            ClimaticZone zone = this.context.getZone(chunkPos);

            this.preventIntersection(2);

            this.decorateScatter(random, chunkPos, writer, this.getOakShrubCount(random, zone), (pos, localX, localZ) -> OAK_TALL_SHRUB.generate(world, random, pos));
            this.decorateScatter(random, chunkPos, writer, this.getJungleShrubCount(random, zone), (pos, localX, localZ) -> JUNGLE_TALL_SHRUB.generate(world, random, pos));

            this.stopIntersectionPrevention();
        }

        private int getOakShrubCount(Random random, ClimaticZone zone) {
            switch (zone) {
                case TROPICS:
                case SUBTROPICS:
                    return this.range(random, 0, 3);
                default:
                    return this.range(random, 2, 5);
            }
        }

        private int getJungleShrubCount(Random random, ClimaticZone zone) {
            switch (zone) {
                case TROPICS:
                case SUBTROPICS:
                    return this.range(random, 2, 5);
                default:
                    return this.range(random, 0, 3);
            }
        }
    }
}
