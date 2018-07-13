package net.gegy1000.terrarium.server.world.pipeline.source;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

// TODO: Interface
public abstract class TiledDataSource<T extends TiledDataAccess> {
    private final LoadingCache<DataTilePos, T> tileCache;

    protected final Coordinate tileSize;

    protected TiledDataSource(Coordinate tileSize, int tileCacheSize) {
        this.tileSize = tileSize;
        this.tileCache = CacheBuilder.newBuilder()
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .maximumSize(tileCacheSize)
                .build(new CacheLoader<DataTilePos, T>() {
                    @Override
                    public T load(DataTilePos key) {
                        try {
                            T tile = TiledDataSource.this.loadTile(key);
                            if (tile != null) {
                                return tile;
                            }
                        } catch (SourceException e) {
                            LoadingStateHandler.countFailure();
                            Terrarium.LOGGER.error("Failed to load from data source", e);
                        }
                        return TiledDataSource.this.getDefaultTile();
                    }
                });
    }

    public Coordinate getTileSize() {
        return this.tileSize;
    }

    public T getTile(DataTilePos key) {
        try {
            return this.tileCache.get(key);
        } catch (ExecutionException e) {
            LoadingStateHandler.countFailure();
            Terrarium.LOGGER.error("Failed to load tile at {}", key, e);
            return this.getDefaultTile();
        }
    }

    public abstract T loadTile(DataTilePos key) throws SourceException;

    protected abstract T getDefaultTile();
}
