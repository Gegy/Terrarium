package net.gegy1000.terrarium.server.map.cover.generator;

import net.gegy1000.terrarium.server.map.cover.CoverGenerator;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.cover.generator.layer.SelectWeightedLayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerFuzzyZoom;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;

import java.util.Random;

public class SnowCover extends CoverGenerator {
    private static final int LAYER_SNOW = 0;
    private static final int LAYER_ICE = 1;

    private GenLayer coverSelector;

    private static final IBlockState SNOW = Blocks.SNOW.getDefaultState();
    private static final IBlockState ICE = Blocks.PACKED_ICE.getDefaultState();

    public SnowCover() {
        super(CoverType.SNOW);
    }

    @Override
    protected void createLayers(boolean debug) {
        GenLayer layer = new SelectWeightedLayer(1,
                new SelectWeightedLayer.Entry(LAYER_SNOW, 15),
                new SelectWeightedLayer.Entry(LAYER_ICE, 3));
        layer = new GenLayerVoronoiZoom(1000, layer);
        layer = new GenLayerFuzzyZoom(2000, layer);
        layer = new GenLayerVoronoiZoom(4300, layer);

        this.coverSelector = layer;
        this.coverSelector.initWorldGenSeed(this.seed);
    }

    @Override
    public void getCover(Random random, int x, int z) {
        this.getCover(random, this.coverBuffer, x, z);
    }

    @Override
    public void getFiller(Random random, int x, int z) {
        this.getCover(random, this.fillerBuffer, x, z);
    }

    private void getCover(Random random, IBlockState[] buffer, int x, int z) {
        this.coverLayer(buffer, x, z, this.coverSelector, type -> {
            int slope = type.getSlope();
            switch (type.getCoverType()) {
                case LAYER_SNOW:
                    return SNOW;
                case LAYER_ICE:
                    return random.nextInt(10) != 0 && slope >= MOUNTAINOUS_SLOPE ? ICE : SNOW;
                default:
                    return SNOW;
            }
        });
    }
}
