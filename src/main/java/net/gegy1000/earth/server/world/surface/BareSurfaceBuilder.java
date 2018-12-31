package net.gegy1000.earth.server.world.surface;

import net.gegy1000.terrarium.server.world.cover.generator.layer.LayerChainer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.gegy1000.terrarium.server.world.surface.DynamicSurfaceBuilder;
import net.minecraft.class_3657;
import net.minecraft.world.biome.layer.CachingLayerSampler;
import net.minecraft.world.biome.layer.ScaleLayer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.Random;

public class BareSurfaceBuilder extends DynamicSurfaceBuilder {
    private static final long SEED = 5087246513514666961L;

    private static final int LAYER_GRAVEL = 0;
    private static final int LAYER_DIRT = 1;
    private static final int LAYER_SAND = 2;

    private final CachingLayerSampler layer;

    public BareSurfaceBuilder() {
        this.layer = LayerChainer.init(SEED, context -> {
            SelectWeightedLayer layer = SelectWeightedLayer.builder()
                    .withEntry(LAYER_GRAVEL, 2)
                    .withEntry(LAYER_DIRT, 10)
                    .withEntry(LAYER_SAND, 5)
                    .build();
            return layer.create(context.apply(1));
        }).apply((layer, context) -> {
            layer = class_3657.INSTANCE.create(context.apply(1000), layer);
            layer = ScaleLayer.NORMAL.create(context.apply(2000), layer);
            return layer;
        }).make();
    }

    // TODO: the config should define the layers
    @Override
    protected TernarySurfaceConfig buildConfig(Random random, Chunk chunk, int x, int z, int y, double surfaceNoise) {
        int value = this.layer.sample(x, z);
        switch (value) {
            case LAYER_GRAVEL:
                return GRAVEL_CONFIG;
            case LAYER_DIRT:
                return DIRT_CONFIG;
            case LAYER_SAND:
                return SAND_CONFIG;
        }
        return null;
    }
}
