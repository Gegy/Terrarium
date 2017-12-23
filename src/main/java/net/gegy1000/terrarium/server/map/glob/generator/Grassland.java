package net.gegy1000.terrarium.server.map.glob.generator;

import net.gegy1000.terrarium.server.map.glob.GlobGenerator;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.glob.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.map.glob.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.map.glob.generator.primer.GlobPrimer;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class Grassland extends GlobGenerator {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;
    private static final int LAYER_PODZOL = 2;

    private static final IBlockState GRASS = Blocks.GRASS.getDefaultState();
    private static final IBlockState DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    private static final IBlockState PODZOL = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);

    private static final IBlockState TALL_GRASS = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
    private static final IBlockState DEAD_BUSH = Blocks.DEADBUSH.getDefaultState();
    private static final IBlockState DOUBLE_TALL_GRASS = Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS);

    private GenLayer coverSelector;
    private GenLayer grassSelector;

    public Grassland() {
        super(GlobType.GRASSLAND);
    }

    @Override
    protected void createLayers() {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new GenLayerVoronoiZoom(1000, cover);
        cover = new ReplaceRandomLayer(LAYER_DIRT, LAYER_PODZOL, 4, 2000, cover);
        cover = new GenLayerFuzzyZoom(3000, cover);

        this.coverSelector = cover;
        this.coverSelector.initWorldGenSeed(this.seed);

        GenLayer grass = new SelectionSeedLayer(2, 3000);
        grass = new GenLayerVoronoiZoom(1000, grass);
        grass = new GenLayerFuzzyZoom(2000, grass);

        this.grassSelector = grass;
        this.grassSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void coverDecorate(GlobPrimer primer, Random random, int x, int z) {
        int[] grassLayer = this.sampleChunk(this.grassSelector, x, z);

        this.iterate(point -> {
            int index = point.index;
            if (grassLayer[index] == 1 && random.nextInt(4) != 0) {
                int y = this.heightBuffer[index];
                if (random.nextInt(4) == 0) {
                    primer.setBlockState(point.localX, y + 1, point.localZ, DOUBLE_TALL_GRASS);
                    primer.setBlockState(point.localX, y + 2, point.localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
                } else if (random.nextInt(16) == 0) {
                    primer.setBlockState(point.localX, y + 1, point.localZ, Grassland.DEAD_BUSH);
                } else {
                    primer.setBlockState(point.localX, y + 1, point.localZ, Grassland.TALL_GRASS);
                }
            }
        });
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.coverLayer(this.coverBuffer, x, z, this.coverSelector, type -> {
            switch (type) {
                case LAYER_GRASS:
                    return GRASS;
                case LAYER_DIRT:
                    return DIRT;
                default:
                    return PODZOL;
            }
        });
    }
}
