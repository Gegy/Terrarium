package net.gegy1000.terrarium.server.world.data.source;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.FutureUtil;
import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class DataSourceReader {
    public static final DataSourceReader INSTANCE = new DataSourceReader();

    private final ExecutorService loadService = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("terrarium-data-loader-%s").setDaemon(true).build());

    private final Cache<TileKey<?>, DataTileResult<?>> tileCache = CacheBuilder.newBuilder()
            .maximumSize(128)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build();

    private final Map<TileKey<?>, CompletableFuture<DataTileResult<?>>> queuedTiles = new HashMap<>();

    private final Object lock = new Object();

    private DataSourceReader() {
    }

    public void clear() {
        this.cancelLoading();
        this.tileCache.invalidateAll();
    }

    public void cancelLoading() {
        for (CompletableFuture<?> future : this.queuedTiles.values()) {
            future.cancel(true);
        }
        this.queuedTiles.clear();
    }

    private <T> CompletableFuture<DataTileResult<?>> enqueueTile(TileKey<T> key) {
        CompletableFuture<DataTileResult<?>> future = CompletableFuture.supplyAsync(() -> this.loadTile(key), this.loadService)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        this.logError(key, throwable);
                        return DataTileResult.empty(key.asVec2());
                    }
                    this.handleResult(key, result);
                    return result;
                });

        this.queuedTiles.put(key, future);

        return future;
    }

    @SuppressWarnings("unchecked")
    public <T> CompletableFuture<DataTileResult<T>> getTile(TiledDataSource<T> source, Vec2i pos) {
        TileKey<T> key = new TileKey<>(source, pos.x, pos.y);
        try {
            DataTileResult<T> result = (DataTileResult<T>) this.tileCache.getIfPresent(key);
            if (result != null) {
                return CompletableFuture.completedFuture(result);
            }

            synchronized (this.lock) {
                CompletableFuture<DataTileResult<?>> future = this.queuedTiles.computeIfAbsent(key, this::enqueueTile);
                return (CompletableFuture<DataTileResult<T>>) (Object) future;
            }
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Unexpected error occurred at {} from {}", pos, source.getClass().getSimpleName(), e);
            ErrorBroadcastHandler.recordFailure();
        }

        return CompletableFuture.completedFuture(DataTileResult.empty(pos));
    }

    public <T> CompletableFuture<Collection<DataTileResult<T>>> getTiles(TiledDataSource<T> source, DataView view) {
        double tileWidth = source.getTileWidth();
        double tileHeight = source.getTileHeight();

        Vec2i minTile = new Vec2i(
                MathHelper.floor(view.getX() / tileWidth),
                MathHelper.floor(view.getY() / tileHeight)
        );
        Vec2i maxTile = new Vec2i(
                MathHelper.floor(view.getMaxX() / tileWidth),
                MathHelper.floor(view.getMaxY() / tileHeight)
        );

        return this.getTiles(source, minTile, maxTile);
    }

    public <T> CompletableFuture<Collection<DataTileResult<T>>> getTiles(
            TiledDataSource<T> source,
            Vec2i min,
            Vec2i max
    ) {
        Collection<Vec2i> tiles = new ArrayList<>((max.x - min.x + 1) * (max.y - min.y + 1));
        for (int y = min.y; y <= max.y; y++) {
            for (int x = min.x; x <= max.x; x++) {
                tiles.add(new Vec2i(x, y));
            }
        }
        return this.getTiles(source, tiles);
    }

    public <T> CompletableFuture<Collection<DataTileResult<T>>> getTiles(TiledDataSource<T> source, Collection<Vec2i> tiles) {
        return FutureUtil.allOf(tiles.stream()
                .map(pos -> this.getTile(source, pos))
                .collect(Collectors.toList())
        );
    }

    private <T> void handleResult(TileKey<T> key, DataTileResult<T> result) {
        synchronized (this.lock) {
            this.tileCache.put(key, result);
            this.queuedTiles.remove(key);
        }
    }

    private <T> DataTileResult<T> loadTile(TileKey<T> key) {
        try {
            Vec2i pos = key.asVec2();
            return new DataTileResult<>(pos, key.source.load(pos));
        } catch (Throwable e) {
            throw new CompletionException(e);
        }
    }

    private <T> void logError(TileKey<T> key, Throwable throwable) {
        String sourceName = key.source.getClass().getSimpleName();
        Terrarium.LOGGER.warn("[{}] Loading tile at {} raised error", sourceName, key.asVec2(), throwable);
        ErrorBroadcastHandler.recordFailure();
    }

    private static class TileKey<T> {
        final TiledDataSource<T> source;
        final int x;
        final int y;

        TileKey(TiledDataSource<T> source, int x, int y) {
            this.source = source;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;

            if (o instanceof TileKey) {
                TileKey key = (TileKey) o;
                return key.source == this.source && key.x == this.x && key.y == this.y;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return 31 * this.x + this.y;
        }

        Vec2i asVec2() {
            return new Vec2i(this.x, this.y);
        }

        @Override
        public String toString() {
            return "TileKey(" + this.x + "; " + this.y + ")";
        }
    }
}
