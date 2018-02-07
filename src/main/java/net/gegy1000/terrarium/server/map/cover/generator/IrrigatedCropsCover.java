package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.layer.ConnectHorizontalLayer;
import net.gegy1000.terrarium.server.map.cover.generator.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.map.cover.generator.primer.GlobPrimer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class IrrigatedCropsCover extends CoverGenerator {
    private static final int CROP_COUNT = 3;

    private static final int LAYER_WHEAT = 0;
    private static final int LAYER_CARROTS = 1;
    private static final int LAYER_POTATOES = 2;

    private static final int LAYER_FENCE = 65535;

    protected static final IBlockState WATER = Blocks.WATER.getDefaultState();
    protected static final IBlockState FARMLAND = Blocks.FARMLAND.getDefaultState().withProperty(BlockFarmland.MOISTURE, 7);
    protected static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);

    private static final IBlockState WHEAT = Blocks.WHEAT.getDefaultState();
    private static final IBlockState CARROTS = Blocks.CARROTS.getDefaultState();
    private static final IBlockState POTATOES = Blocks.POTATOES.getDefaultState();

    private static final IBlockState FENCE = Blocks.OAK_FENCE.getDefaultState();

    private GenLayer cropSelector;

    public IrrigatedCropsCover() {
        super(CoverType.IRRIGATED_CROPS);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer layer = new SelectionSeedLayer(IrrigatedCropsCover.CROP_COUNT, 1);
        layer = new GenLayerVoronoiZoom(1000, layer);
        layer = new GenLayerFuzzyZoom(2000, layer);
        layer = new GenLayerVoronoiZoom(3000, layer);
        layer = new OutlineEdgeLayer(IrrigatedCropsCover.LAYER_FENCE, 4000, layer);
        layer = new ConnectHorizontalLayer(IrrigatedCropsCover.LAYER_FENCE, 5000, layer);

        this.cropSelector = layer;
        this.cropSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] cropLayer = this.sampleChunk(this.cropSelector, x, z);
        this.iterate(point -> {
            int y = this.heightBuffer[point.index];
            IBlockState state = IrrigatedCropsCover.getCropState(cropLayer[point.index]);

            if (state.getBlock() instanceof BlockCrops) {
                if (random.nextInt(20) != 0) {
                    if (primer.getBlockState(point.localX, y, point.localZ).getBlock() instanceof BlockFarmland) {
                        primer.setBlockState(point.localX, y + 1, point.localZ, state.withProperty(BlockCrops.AGE, random.nextInt(8)));
                    }
                }
            } else {
                primer.setBlockState(point.localX, y, point.localZ, IrrigatedCropsCover.COARSE_DIRT);
                primer.setBlockState(point.localX, y + 1, point.localZ, state);
            }
        });
    }

    @Override
    protected IBlockState getCoverAt(Random random, int x, int z, int slope) {
        if (x % 9 == 0 && z % 9 == 0) {
            return WATER;
        }
        if (random.nextInt(60) == 0) {
            return COARSE_DIRT;
        }
        return FARMLAND;
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
