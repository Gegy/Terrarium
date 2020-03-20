package net.gegy1000.terrarium.server.world.data.source;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.justnow.Waker;
import net.gegy1000.justnow.future.BlockingTaskHandle;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.justnow.tuple.Unit;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public final class DataSourceReader {
    public static final DataSourceReader INSTANCE = new DataSourceReader();

    private final ExecutorService loadService = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setNameFormat("terrarium-data-loader-%s").setDaemon(true).build());

    private final Cache<TileKey<?>, DataTileResult<?>> tileCache = CacheBuilder.newBuilder()
            .maximumSize(128)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build();

    private final Map<TileKey<?>, BlockingTaskHandle<DataTileResult<?>>> queuedTiles = new HashMap<>();

    private final LinkedBlockingDeque<Waker> queueEmpty = new LinkedBlockingDeque<>();

    private final Object lock = new Object();

    private DataSourceReader() {
    }

    public Future<Unit> finishLoading() {
        return waker -> {
            this.queueEmpty.add(waker);
            if (this.queuedTiles.isEmpty()) {
                return Unit.INSTANCE;
            } else {
                return null;
            }
        };
    }

    private void notifyQueueEmpty() {
        while (!this.queueEmpty.isEmpty()) {
            Waker waker = this.queueEmpty.remove();
            waker.wake();
        }
    }

    public void clear() {
        this.cancelLoading();
        this.tileCache.invalidateAll();
    }

    public void cancelLoading() {
        for (BlockingTaskHandle<?> handle : this.queuedTiles.values()) {
            handle.cancel();
        }
        this.queuedTiles.clear();
        this.notifyQueueEmpty();
    }

    private <T> BlockingTaskHandle<DataTileResult<?>> enqueueTile(TileKey<T> key) {
        return Future.spawnBlocking(this.loadService, () -> {
            try {
                DataTileResult<T> tile = this.loadTile(key);
                this.handleResult(key, tile);
                return tile;
            } catch (Throwable t) {
                this.logError(key, t);
                return DataTileResult.empty(key.asVec2());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> Future<DataTileResult<T>> getTile(TiledDataSource<T> source, Vec2i pos) {
        TileKey<T> key = new TileKey<>(source, pos.x, pos.y);
        try {
            DataTileResult<T> result = (DataTileResult<T>) this.tileCache.getIfPresent(key);
            if (result != null) {
                return Future.ready(result);
            }

            synchronized (this.lock) {
                BlockingTaskHandle<DataTileResult<?>> future = this.queuedTiles.computeIfAbsent(key, this::enqueueTile);
                return (Future<DataTileResult<T>>) (Future) future;
            }
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Unexpected error occurred at {} from {}", pos, source.getClass().getSimpleName(), e);
            ErrorBroadcastHandler.recordFailure();
        }

        return Future.ready(DataTileResult.empty(pos));
    }

    public <T> Future<Collection<DataTileResult<T>>> getTiles(TiledDataSource<T> source, DataView view) {
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

    public <T> Future<Collection<DataTileResult<T>>> getTiles(
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

    public <T> Future<Collection<DataTileResult<T>>> getTiles(TiledDataSource<T> source, Collection<Vec2i> tiles) {
        return Future.joinAll(tiles.stream().map(pos -> this.getTile(source, pos)));
    }

    private <T> void handleResult(TileKey<T> key, DataTileResult<T> result) {
        synchronized (this.lock) {
            this.tileCache.put(key, result);
            this.queuedTiles.remove(key);
        }
        if (this.queuedTiles.isEmpty()) {
            this.notifyQueueEmpty();
        }
    }

    private <T> DataTileResult<T> loadTile(TileKey<T> key) throws IOException {
        Vec2i pos = key.asVec2();
        return new DataTileResult<>(pos, key.source.load(pos));
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
            return "TileKey(" + this.x + "; " + this.y + ") @ " + this.source;
        }
    }
}
