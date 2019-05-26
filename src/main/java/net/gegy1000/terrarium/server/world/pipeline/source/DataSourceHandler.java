package net.gegy1000.terrarium.server.world.pipeline.source;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.FutureUtil;
import net.gegy1000.terrarium.server.world.pipeline.data.Data;

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

public enum DataSourceHandler {
    INSTANCE;

    private final ExecutorService loadService = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("terrarium-data-loader-%s").setDaemon(true).build());

    private final Cache<DataTileKey<?>, Data> tileCache = CacheBuilder.newBuilder()
            .maximumSize(32)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build();

    private final Map<DataTileKey<?>, CompletableFuture<?>> queuedTiles = new HashMap<>();

    private final Object lock = new Object();

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

    private <T extends Data> CompletableFuture<T> enqueueTile(DataTileKey<T> key) {
        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> this.loadTile(key), this.loadService)
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        this.logError(key, throwable);
                        return key.getSource().getDefaultResult();
                    }
                    this.handleResult(key, result);
                    return result;
                });

        this.queuedTiles.put(key, future);

        return future;
    }

    @SuppressWarnings("unchecked")
    public <T extends Data> CompletableFuture<T> getTile(TiledDataSource<T> source, DataTilePos pos) {
        DataTileKey<T> key = new DataTileKey<>(source, pos.getTileX(), pos.getTileZ());
        try {
            T result = (T) this.tileCache.getIfPresent(key);
            if (result != null) {
                return CompletableFuture.completedFuture(result);
            }

            synchronized (this.lock) {
                return (CompletableFuture<T>) this.queuedTiles.computeIfAbsent(key, this::enqueueTile);
            }
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Unexpected error occurred at {} from {}", pos, source.getClass().getSimpleName(), e);
            ErrorBroadcastHandler.recordFailure();
        }

        return CompletableFuture.completedFuture(source.getDefaultResult());
    }

    public <T extends Data> CompletableFuture<Collection<DataTileEntry<T>>> getTiles(
            TiledDataSource<T> source,
            DataTilePos min,
            DataTilePos max
    ) {
        Collection<DataTilePos> tiles = new ArrayList<>();
        for (int tileZ = min.getTileZ(); tileZ <= max.getTileZ(); tileZ++) {
            for (int tileX = min.getTileX(); tileX <= max.getTileX(); tileX++) {
                tiles.add(new DataTilePos(tileX, tileZ));
            }
        }

        return this.getTiles(source, tiles);
    }

    public <T extends Data> CompletableFuture<Collection<DataTileEntry<T>>> getTiles(TiledDataSource<T> source, Collection<DataTilePos> tilePositions) {
        Collection<CompletableFuture<DataTileEntry<T>>> tiles = tilePositions.stream()
                .map(pos -> {
                    CompletableFuture<T> future = this.getTile(source, pos);
                    return future.thenApply(t -> new DataTileEntry<>(pos, t));
                })
                .collect(Collectors.toList());

        return FutureUtil.allOf(tiles);
    }

    private <T extends Data> void handleResult(DataTileKey<T> key, T result) {
        synchronized (this.lock) {
            this.tileCache.put(key, result);
            this.queuedTiles.remove(key);
        }
    }

    private <T extends Data> T loadTile(DataTileKey<T> key) {
        TiledDataSource<T> source = key.getSource();
        try {
            return source.load(key.toPos())
                    .orElseGet(source::getDefaultResult);
        } catch (Throwable e) {
            throw new CompletionException(e);
        }
    }

    private <T extends Data> void logError(DataTileKey<T> key, Throwable throwable) {
        TiledDataSource<T> source = key.getSource();
        String sourceName = source.getClass().getSimpleName();
        Terrarium.LOGGER.warn("[{}] Loading tile at {} rose error", sourceName, key.toPos(), throwable);
        ErrorBroadcastHandler.recordFailure();
    }
}
