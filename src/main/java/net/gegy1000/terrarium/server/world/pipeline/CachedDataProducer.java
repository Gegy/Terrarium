package net.gegy1000.terrarium.server.world.pipeline;

import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

import java.util.HashMap;
import java.util.Map;

public abstract class CachedDataProducer<T extends TiledDataAccess> implements DataLayerProducer<T> {
    private final Map<DataView, T> cache = new HashMap<>();

    @Override
    public void reset() {
        this.cache.clear();
    }

    @Override
    public final T apply(DataView view) {
        T cached = this.cache.get(view);
        if (cached != null) {
            return cached;
        }

        T result = this.create(view);
        this.cache.put(view, result);
        return result;
    }

    public abstract T create(DataView view);
}
