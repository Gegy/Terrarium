package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class CachedLayerContext implements LayerContext {
    private final DataSourceHandler sourceHandler;
    private final Map<DataLayer<?>, LayerCache<?>> cache = new IdentityHashMap<>();

    public CachedLayerContext(DataSourceHandler sourceHandler) {
        this.sourceHandler = sourceHandler;
    }

    @Override
    public <T extends TiledDataAccess> T apply(DataLayer<T> layer, DataView view) {
        LayerCache<T> cache = this.getCache(layer);
        T cachedData = cache.getCachedData(view);
        if (cachedData != null) {
            return cachedData;
        }

        T data = layer.apply(this, view);
        cache.cacheData(view, data);
        return data;
    }

    @Override
    public DataSourceHandler getSourceHandler() {
        return this.sourceHandler;
    }

    @SuppressWarnings("unchecked")
    private <T extends TiledDataAccess> LayerCache<T> getCache(DataLayer<T> layer) {
        return (LayerCache<T>) this.cache.computeIfAbsent(layer, dataLayer -> new LayerCache<>());
    }

    private static class LayerCache<T extends TiledDataAccess> {
        private final Map<DataView, T> dataCache = new HashMap<>();

        void cacheData(DataView view, T data) {
            this.dataCache.put(view, data);
        }

        @Nullable
        T getCachedData(DataView view) {
            return this.dataCache.get(view);
        }
    }
}
