package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

import java.util.Random;

public class FloodedGrasslandCover extends EarthCoverType {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;
    private static final int LAYER_PODZOL = 2;

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
        return CoverBiomeSelectors.FLOODED_SELECTOR.apply(context.getZone(x, z));
    }

    private static class Surface extends EarthSurfaceGenerator {
        private final GenLayer coverSelector;
        private final GenLayer waterSelector;
        private final GenLayer grassSelector;

        private Surface(EarthCoverContext context, CoverType<EarthCoverContext> coverType) {
            super(context, coverType);

            GenLayer cover = new SelectionSeedLayer(2, 1);
            cover = new ReplaceRandomLayer(LAYER_GRASS, LAYER_DIRT, 2, 6000, cover);
            cover = new GenLayerVoronoiZoom(7000, cover);
            cover = new ReplaceRandomLayer(LAYER_DIRT, LAYER_PODZOL, 3, 8000, cover);
            cover = new GenLayerFuzzyZoom(9000, cover);

            this.coverSelector = cover;
            this.coverSelector.initWorldGenSeed(context.getSeed());

            GenLayer water = new SelectionSeedLayer(2, 2);
            water = new GenLayerFuzzyZoom(11000, water);
            water = new GenLayerVoronoiZoom(12000, water);
            water = new OutlineEdgeLayer(3, 13000, water);
            water = new GenLayerZoom(14000, water);

            this.waterSelector = water;
            this.waterSelector.initWorldGenSeed(context.getSeed());

            GenLayer grass = new SelectionSeedLayer(2, 3000);
            grass = new GenLayerVoronoiZoom(1000, grass);
            grass = new GenLayerFuzzyZoom(2000, grass);

            this.grassSelector = grass;
            this.grassSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            int[] cover = this.sampleChunk(this.coverSelector, originX, originZ);
            int[] water = this.sampleChunk(this.waterSelector, originX, originZ);
            this.iterateChunk((localX, localZ) -> {
                int index = localX + localZ * 16;
                if (water[index] == 3) {
                    coverBlockBuffer[index] = WATER;
                } else {
                    switch (cover[index]) {
                        case LAYER_GRASS:
                            coverBlockBuffer[index] = GRASS;
                            break;
                        case LAYER_PODZOL:
                            coverBlockBuffer[index] = PODZOL;
                            break;
                        default:
                            coverBlockBuffer[index] = COARSE_DIRT;
                            break;
                    }
                }
            });
        }

        @Override
        public void populateBlockFiller(Random random, int originX, int originZ, IBlockState[] fillerBlockBuffer) {
            this.coverBlock(fillerBlockBuffer, COARSE_DIRT);
        }

        @Override
        public void decorate(CubicPos chunkPos, ChunkPrimeWriter writer, Random random) {
            ShortRasterTile heightRaster = this.context.getHeightRaster();

            int[] grassLayer = this.sampleChunk(this.grassSelector, chunkPos);

            this.iterateChunk((localX, localZ) -> {
                int index = localX + localZ * 16;
                if (grassLayer[index] == 1 && random.nextInt(6) != 0) {
                    int y = heightRaster.getShort(localX, localZ);
                    IBlockState ground = writer.get(localX, y, localZ);
                    if (ground.getBlock() instanceof BlockLiquid) {
                        if (random.nextInt(3) == 0) {
                            writer.set(localX, y + 1, localZ, LILYPAD);
                        }
                    } else if (random.nextInt(3) != 0) {
                        writer.set(localX, y + 1, localZ, TALL_GRASS);
                    } else {
                        writer.set(localX, y + 1, localZ, DOUBLE_TALL_GRASS);
                        writer.set(localX, y + 2, localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
                    }
                }
            });
        }
    }
}
