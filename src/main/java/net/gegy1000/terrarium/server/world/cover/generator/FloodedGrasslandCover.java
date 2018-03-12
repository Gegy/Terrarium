package net.gegy1000.terrarium.server.world.cover.generator;

import net.gegy1000.terrarium.server.world.cover.CoverGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.generator.layer.OutlineEdgeLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.ReplaceRandomLayer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectionSeedLayer;
import net.gegy1000.terrarium.server.world.cover.generator.primer.GlobPrimer;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import net.minecraft.world.gen.layer.GenLayerZoom;

import java.util.Random;

public class FloodedGrasslandCover extends CoverGenerator {
    private static final int LAYER_GRASS = 0;
    private static final int LAYER_DIRT = 1;
    private static final int LAYER_PODZOL = 2;

    private GenLayer coverSelector;
    private GenLayer waterSelector;
    private GenLayer grassSelector;

    public FloodedGrasslandCover() {
        super(CoverType.FLOODED_GRASSLAND);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer cover = new SelectionSeedLayer(2, 1);
        cover = new ReplaceRandomLayer(LAYER_GRASS, LAYER_DIRT, 2, 6000, cover);
        cover = new GenLayerVoronoiZoom(7000, cover);
        cover = new ReplaceRandomLayer(LAYER_DIRT, LAYER_PODZOL, 3, 8000, cover);
        cover = new GenLayerFuzzyZoom(9000, cover);

        this.coverSelector = cover;
        this.coverSelector.initWorldGenSeed(this.seed);

        GenLayer water = new SelectionSeedLayer(2, 2);
        water = new GenLayerFuzzyZoom(11000, water);
        water = new GenLayerVoronoiZoom(12000, water);
        water = new OutlineEdgeLayer(3, 13000, water);
        water = new GenLayerZoom(14000, water);

        this.waterSelector = water;
        this.waterSelector.initWorldGenSeed(this.seed);

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
            if (grassLayer[index] == 1 && random.nextInt(6) != 0) {
                int y = this.heightBuffer[index];
                IBlockState ground = primer.getBlockState(point.localX, y, point.localZ);
                if (ground.getBlock() instanceof BlockLiquid) {
                    if (random.nextInt(3) == 0) {
                        primer.setBlockState(point.localX, y + 1, point.localZ, LILYPAD);
                    }
                } else if (random.nextInt(3) != 0) {
                    primer.setBlockState(point.localX, y + 1, point.localZ, TALL_GRASS);
                } else {
                    primer.setBlockState(point.localX, y + 1, point.localZ, DOUBLE_TALL_GRASS);
                    primer.setBlockState(point.localX, y + 2, point.localZ, DOUBLE_TALL_GRASS.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER));
                }
            }
        });
    }

    @Override
    public void getCover(Random random, int x, int z) {
        int[] cover = this.sampleChunk(this.coverSelector, x, z);
        int[] water = this.sampleChunk(this.waterSelector, x, z);
        this.iterate(point -> {
            int index = point.index;
            if (water[index] == 3) {
                this.coverBlockBuffer[index] = WATER;
            } else {
                switch (cover[index]) {
                    case LAYER_GRASS:
                        this.coverBlockBuffer[index] = GRASS;
                        break;
                    case LAYER_PODZOL:
                        this.coverBlockBuffer[index] = PODZOL;
                        break;
                    default:
                        this.coverBlockBuffer[index] = COARSE_DIRT;
                        break;
                }
            }
        });
    }

    @Override
    protected IBlockState getFillerAt(Random random, int x, int z, int slope) {
        return COARSE_DIRT;
    }
}
