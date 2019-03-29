package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthDecorationGenerator;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.awt.Color;
import java.util.Random;

public class SparseVegetationCover extends EarthCoverType {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;

    public SparseVegetationCover() {
        super(new Color(0xFFEBAE));
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
        return CoverBiomeSelectors.GRASSLAND_SELECTOR.apply(context.getZone(x, z));
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer coverSelector;
        private final GenLayer grassSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer cover = new SelectionSeedLayer(2, 1);
            cover = new GenLayerVoronoiZoom(1000, cover);
            cover = new GenLayerFuzzyZoom(3000, cover);

            this.coverSelector = cover;
            this.coverSelector.initWorldGenSeed(context.getSeed());

            GenLayer grass = new SelectionSeedLayer(3, 3000);
            grass = new GenLayerVoronoiZoom(1000, grass);
            grass = new GenLayerFuzzyZoom(2000, grass);

            this.grassSelector = grass;
            this.grassSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.coverFromLayer(coverBlockBuffer, originX, originZ, this.coverSelector, (sampledValue, localX, localZ) -> {
                switch (sampledValue) {
                    case LAYER_GRASS:
                        return GRASS;
                    case LAYER_DIRT:
                        return COARSE_DIRT;
                    default:
                        return GRASS;
                }
            });
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
            ShortRaster heightRaster = this.context.getHeightRaster();
            int[] grassLayer = this.sampleChunk(this.grassSelector, chunkPos);

            this.iterateChunk((localX, localZ) -> {
                int y = heightRaster.getShort(localX, localZ);
                switch (grassLayer[localX + localZ * 16]) {
                    case 0:
                        if (random.nextInt(8) == 0) {
                            IBlockState ground = writer.get(localX, y, localZ);
                            if (ground == COARSE_DIRT) {
                                writer.set(localX, y + 1, localZ, TALL_GRASS);
                            } else if (random.nextInt(6) == 0) {
                                writer.set(localX, y + 1, localZ, DEAD_BUSH);
                            }
                        }
                        break;
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

            this.preventIntersection(2);

            this.decorateScatter(random, chunkPos, writer, this.range(random, -5, 2), (pos, localX, localZ) -> OAK_TALL_SHRUB.generate(world, random, pos));
            this.decorateScatter(random, chunkPos, writer, this.range(random, -5, 2), (pos, localX, localZ) -> JUNGLE_TALL_SHRUB.generate(world, random, pos));

            this.stopIntersectionPrevention();
        }
    }
}
