package net.gegy1000.terrarium.server.world.cover.generator.layer;

import net.minecraft.world.biome.layer.CachingLayerContext;
import net.minecraft.world.biome.layer.CachingLayerSampler;
import net.minecraft.world.biome.layer.LayerFactory;

import java.util.function.LongFunction;

public class LayerChainer {
    private final long seed;
    private LayerFactory<CachingLayerSampler> factory;

    private LayerChainer(long seed, LayerFactory<CachingLayerSampler> factory) {
        this.seed = seed;
        this.factory = factory;
    }

    public static LayerChainer init(long seed, Initializer initializer) {
        return new LayerChainer(seed, initializer.initialize(getContextSupplier(seed)));
    }

    public LayerChainer apply(Chain chain) {
        this.factory = chain.chain(this.factory, getContextSupplier(this.seed));
        return this;
    }

    private static LongFunction<CachingLayerContext> getContextSupplier(long globalSeed) {
        return seed -> new CachingLayerContext(25, globalSeed, seed);
    }

    public CachingLayerSampler make() {
        return this.factory.make();
    }

    public interface Initializer {
        LayerFactory<CachingLayerSampler> initialize(LongFunction<CachingLayerContext> context);
    }

    public interface Chain {
        LayerFactory<CachingLayerSampler> chain(LayerFactory<CachingLayerSampler> layer, LongFunction<CachingLayerContext> context);
    }
}
