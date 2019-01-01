package net.gegy1000.earth.server.world.surface;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.gegy1000.terrarium.server.world.cover.generator.layer.LayerChainer;
import net.gegy1000.terrarium.server.world.cover.generator.layer.SelectWeightedLayer;
import net.minecraft.class_3657;
import net.minecraft.world.biome.layer.CachingLayerSampler;
import net.minecraft.world.biome.layer.ScaleLayer;
import net.minecraft.world.gen.surfacebuilder.TernarySurfaceConfig;

import java.util.function.IntFunction;

public class PatchedSurfaceConfig extends LayerSurfaceConfig {
    private PatchedSurfaceConfig(CachingLayerSampler sampler, IntFunction<TernarySurfaceConfig> surfaceSelector) {
        super(sampler, surfaceSelector);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SelectWeightedLayer.Builder layerBuilder = SelectWeightedLayer.builder();
        private final Int2ObjectMap<TernarySurfaceConfig> configs = new Int2ObjectOpenHashMap<>();
        private int id;

        private Builder() {
        }

        public Builder withEntry(TernarySurfaceConfig config, int weight) {
            int id = this.id++;
            this.layerBuilder.withEntry(id, weight);
            this.configs.put(id, config);
            return this;
        }

        public PatchedSurfaceConfig build() {
            CachingLayerSampler sampler = LayerChainer.init(0, context -> {
                return this.layerBuilder.build().create(context.apply(1));
            }).apply((layer, context) -> {
                layer = class_3657.INSTANCE.create(context.apply(1000), layer);
                layer = ScaleLayer.NORMAL.create(context.apply(2000), layer);
                return layer;
            }).make();
            return new PatchedSurfaceConfig(sampler, this.configs::get);
        }
    }
}
