package net.gegy1000.terrarium.server.map.source.tiled;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.source.DataSource;
import net.gegy1000.terrarium.server.map.source.LoadingState;
import net.gegy1000.terrarium.server.map.source.LoadingStateHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class TiledSource<T extends TiledDataAccess> implements DataSource {
    private final LoadingCache<DataTilePos, T> tileCache;

    protected final double tileSize;

    protected TiledSource(double tileSize, int tileCacheSize) {
        this.tileSize = tileSize;
        this.tileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .maximumSize(tileCacheSize)
                .build(new CacheLoader<DataTilePos, T>() {
                    @Override
                    public T load(DataTilePos key) {
                        T tile = TiledSource.this.loadTile(key);
                        return tile != null ? tile : TiledSource.this.getDefaultTile();
                    }
                });
    }

    public double getTileSize() {
        return this.tileSize;
    }

    public T getTile(DataTilePos key) {
        try {
            return this.tileCache.get(key);
        } catch (ExecutionException e) {
            LoadingStateHandler.putState(LoadingState.LOADING_NO_CONNECTION);
            Terrarium.LOGGER.error("Failed to load tile at {}", key, e);
            return this.getDefaultTile();
        }
    }

    public abstract T loadTile(DataTilePos key);

    protected abstract T getDefaultTile();
}
