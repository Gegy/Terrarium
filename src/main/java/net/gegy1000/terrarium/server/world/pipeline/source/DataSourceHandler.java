package net.gegy1000.terrarium.server.world.pipeline.source;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.GenerationCancelledException;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public enum DataSourceHandler {
    INSTANCE;

    private final ExecutorService loadService = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("terrarium-data-loader-%s").setDaemon(true).build());
    private final ExecutorService cacheService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("terrarium-cache-service").setDaemon(true).build());

    private final Cache<DataTileKey<?>, TiledDataAccess> tileCache = CacheBuilder.newBuilder()
            .maximumSize(32)
            .expireAfterAccess(30, TimeUnit.SECONDS)
            .build();

    private final Map<DataTileKey<?>, TileFuture<?>> queuedTiles = new HashMap<>();

    private final Object lock = new Object();

    public void clear() {
        this.cancelLoading();
        this.tileCache.invalidateAll();
    }

    public void cancelLoading() {
        for (TileFuture<?> future : this.queuedTiles.values()) {
            future.cancel();
        }
        this.queuedTiles.clear();
    }

    public void enqueueData(Set<DataTileKey<?>> requiredData) {
        for (DataTileKey<?> key : requiredData) {
            if (this.shouldQueueData(key)) {
                synchronized (this.lock) {
                    this.enqueueTile(key);
                }
            }
        }
    }

    private boolean shouldQueueData(DataTileKey<?> key) {
        synchronized (this.lock) {
            return !this.queuedTiles.containsKey(key) && this.tileCache.getIfPresent(key) == null;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends TiledDataAccess> TileFuture<T> enqueueTile(DataTileKey<T> key) {
        TileFuture<T> future = new TileFuture<>(key);
        future.submitTo(this.loadService);
        this.queuedTiles.put(key, future);
        return future;
    }

    @SuppressWarnings("unchecked")
    public <T extends TiledDataAccess> T getTile(TiledDataSource<T> source, DataTilePos pos) {
        try {
            this.collectCompletedTiles();
        } catch (InterruptedException e) {
            throw new GenerationCancelledException(e);
        }

        DataTileKey<T> key = new DataTileKey<>(source, pos.getTileX(), pos.getTileZ());
        try {
            T result = (T) this.tileCache.getIfPresent(key);
            if (result != null) {
                return result;
            }

            TileFuture<T> tileFuture;
            synchronized (this.lock) {
                tileFuture = (TileFuture<T>) this.queuedTiles.get(key);
                if (tileFuture == null) {
                    tileFuture = this.enqueueTile(key);
                }
            }

            T loadedResult = this.parseResult(tileFuture);

            synchronized (this.lock) {
                this.queuedTiles.remove(key);
                this.tileCache.put(key, loadedResult);
            }

            return loadedResult;
        } catch (InterruptedException e) {
            throw new GenerationCancelledException(e);
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Unexpected exception occurred at {} from {}", pos, source.getIdentifier(), e);
            LoadingStateHandler.recordFailure();
        }

        return source.getDefaultTile();
    }

    private void collectCompletedTiles() throws InterruptedException {
        if (this.queuedTiles.isEmpty()) {
            return;
        }

        Set<TileFuture<?>> completedTiles = new HashSet<>();

        synchronized (this.lock) {
            for (TileFuture<?> future : this.queuedTiles.values()) {
                if (future.isComplete()) {
                    completedTiles.add(future);
                }
            }
        }

        for (TileFuture<?> future : completedTiles) {
            TiledDataAccess parsedResult = this.parseResult(future);
            this.tileCache.put(future.key, parsedResult);
            synchronized (this.lock) {
                this.queuedTiles.remove(future.key);
            }
        }
    }

    private <T extends TiledDataAccess> T parseResult(TileFuture<T> future) throws InterruptedException {
        DataTileKey<T> key = future.key;
        SourceResult<T> result = future.getResult();
        if (result.isError()) {
            Terrarium.LOGGER.warn("Loading tile at {} from {} gave error {}: {}", key.toPos(), key.getSource().getIdentifier(), result.getError(), result.getErrorCause());
            LoadingStateHandler.recordFailure();
            return key.getSource().getDefaultTile();
        }
        T value = result.getValue();
        if (value == null) {
            return key.getSource().getDefaultTile();
        }
        return value;
    }

    private <T extends TiledDataAccess> SourceResult<T> loadTileRobustly(DataTileKey<T> key) {
        SourceResult<T> result = this.loadTile(key);
        if (result.isError() && result.getError() == SourceResult.Error.MALFORMED) {
            TiledDataSource<T> source = key.getSource();
            File cachedFile = new File(source.getCacheRoot(), source.getCachedName(key.toPos()));
            if (cachedFile.delete()) {
                return this.loadTile(key);
            }
        }
        return result;
    }

    private <T extends TiledDataAccess> SourceResult<T> loadTile(DataTileKey<T> key) {
        TiledDataSource<T> source = key.getSource();
        T localTile = source.getLocalTile(key.toPos());
        if (localTile != null) {
            return SourceResult.success(localTile);
        }

        DataTilePos loadPos = source.getLoadTilePos(key.toPos());
        File cachedFile = new File(source.getCacheRoot(), source.getCachedName(loadPos));
        if (!source.shouldLoadCache(loadPos, cachedFile)) {
            return this.loadRemoteTile(source, loadPos, cachedFile);
        } else {
            return this.loadCachedTile(source, loadPos, cachedFile);
        }
    }

    private <T extends TiledDataAccess> SourceResult<T> loadRemoteTile(TiledDataSource<T> source, DataTilePos pos, File cachedFile) {
        LoadingStateHandler.pushState(LoadingState.LOADING_REMOTE);
        try (InputStream remoteStream = source.getRemoteStream(pos)) {
            InputStream cachingStream = this.getCachingStream(remoteStream, cachedFile);
            source.cacheMetadata(pos);
            return source.parseStream(pos, source.getWrappedStream(cachingStream));
        } catch (EOFException e) {
            return SourceResult.malformed("Reached end of file before expected");
        } catch (IOException e) {
            return SourceResult.exception(e);
        } finally {
            LoadingStateHandler.popState();
        }
    }

    private <T extends TiledDataAccess> SourceResult<T> loadCachedTile(TiledDataSource<T> source, DataTilePos pos, File cachedFile) {
        LoadingStateHandler.pushState(LoadingState.LOADING_CACHED);
        try {
            return source.parseStream(pos, source.getWrappedStream(new BufferedInputStream(new FileInputStream(cachedFile))));
        } catch (EOFException e) {
            return SourceResult.malformed("Reached end of file before expected");
        } catch (IOException e) {
            return SourceResult.exception(e);
        } finally {
            LoadingStateHandler.popState();
        }
    }

    private InputStream getCachingStream(InputStream source, File cacheFile) throws IOException {
        PipedOutputStream sink = new PipedOutputStream();
        InputStream input = new PipedInputStream(sink);
        this.cacheService.submit(() -> {
            try (OutputStream file = new FileOutputStream(cacheFile)) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = source.read(buffer)) != IOUtils.EOF) {
                    file.write(buffer, 0, count);
                    sink.write(buffer, 0, count);
                }
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to read or cache remote data", e);
                if (cacheFile.exists()) {
                    cacheFile.delete();
                }
            } finally {
                IOUtils.closeQuietly(sink);
            }
        });
        return input;
    }

    public void close() {
        this.loadService.shutdown();
        this.cacheService.shutdown();
    }

    private class TileFuture<T extends TiledDataAccess> {
        private final DataTileKey<T> key;
        private Future<SourceResult<T>> future;

        private TileFuture(DataTileKey<T> key) {
            this.key = key;
        }

        public boolean isComplete() {
            return this.future.isDone() || this.future.isCancelled();
        }

        public SourceResult<T> getResult() throws InterruptedException {
            try {
                if (this.future == null) {
                    return SourceResult.empty();
                }
                return this.future.get();
            } catch (ExecutionException e) {
                return SourceResult.exception(e);
            }
        }

        public void submitTo(ExecutorService service) {
            if (!service.isShutdown()) {
                this.future = service.submit(() -> DataSourceHandler.this.loadTileRobustly(this.key));
            }
        }

        public void cancel() {
            this.future.cancel(true);
        }
    }
}
