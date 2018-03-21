package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthSurfaceGenerator;
import net.gegy1000.earth.server.world.cover.LatitudinalZone;
import net.gegy1000.terrarium.server.world.cover.CoverBiomeSelectors;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ConnectHorizontalLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.CoverPrimer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class IrrigatedCropsCover implements CoverType {
    private static final int CROP_COUNT = 3;

    private static final int LAYER_WHEAT = 0;
    private static final int LAYER_CARROTS = 1;
    private static final int LAYER_POTATOES = 2;

    private static final int LAYER_FENCE = 65535;

    protected static final IBlockState FARMLAND = Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, 7);

    private static final IBlockState WHEAT = Blocks.WHEAT.getDefaultState();
    private static final IBlockState CARROTS = Blocks.CARROTS.getDefaultState();
    private static final IBlockState POTATOES = Blocks.POTATOES.getDefaultState();

    private static final IBlockState FENCE = Blocks.OAK_FENCE.getDefaultState();

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
        private final GenLayer cropSelector;

        private Surface(CoverGenerationContext context, CoverType coverType) {
            super(context, coverType);

            GenLayer layer = new SelectionSeedLayer(CROP_COUNT, 1);
            layer = new GenLayerVoronoiZoom(1000, layer);
            layer = new GenLayerFuzzyZoom(2000, layer);
            layer = new GenLayerVoronoiZoom(3000, layer);
            layer = new OutlineEdgeLayer(LAYER_FENCE, 4000, layer);
            layer = new ConnectHorizontalLayer(LAYER_FENCE, 5000, layer);

            this.cropSelector = layer;
            this.cropSelector.initWorldGenSeed(context.getSeed());
        }

        @Override
        public void populateBlockCover(Random random, int originX, int originZ, IBlockState[] coverBlockBuffer) {
            this.iterateChunk((localX, localZ) -> {
                int globalX = originX + localX;
                int globalZ = originZ + localZ;

                int index = localX + localZ * 16;
                if (globalX % 9 == 0 && globalZ % 9 == 0) {
                    coverBlockBuffer[index] = WATER;
                } else if (random.nextInt(60) == 0) {
                    coverBlockBuffer[index] = COARSE_DIRT;
                } else {
                    coverBlockBuffer[index] = FARMLAND;
                }
            });
        }

        @Override
        public void decorate(int originX, int originZ, CoverPrimer primer, Random random) {
            ShortRasterTileAccess heightRaster = this.context.getHeightRaster();
            int[] cropLayer = this.sampleChunk(this.cropSelector, originX, originZ);

            this.iterateChunk((localX, localZ) -> {
                int y = heightRaster.getShort(localX, localZ);
                IBlockState state = getCropState(cropLayer[localX + localZ * 16]);

                if (state.getBlock() instanceof BlockCrops) {
                    if (random.nextInt(20) != 0) {
                        if (primer.getBlockState(localX, y, localZ).getBlock() instanceof BlockFarmland) {
                            primer.setBlockState(localX, y + 1, localZ, state.withProperty(BlockCrops.AGE, random.nextInt(8)));
                        }
                    }
                } else {
                    primer.setBlockState(localX, y, localZ, COARSE_DIRT);
                    primer.setBlockState(localX, y + 1, localZ, state);
                }
            });
        }

        protected static IBlockState getCropState(int id) {
            switch (id) {
                case LAYER_WHEAT:
                    return WHEAT;
                case LAYER_CARROTS:
                    return CARROTS;
                case LAYER_POTATOES:
                    return POTATOES;
                default:
                    return FENCE;
            }
        }
    }
}
