package net.gegy1000.terrarium.server.world.pipeline.source;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
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

public class DataSourceHandler {
    private final ExecutorService loadService = Executors.newFixedThreadPool(2, new ThreadFactoryBuilder().setNameFormat("terrarium-data-loader-%s").setDaemon(true).build());
    private final ExecutorService cacheService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("terrarium-cache-service").setDaemon(true).build());

    private final Cache<DataTileKey<?>, TiledDataAccess> tileCache = CacheBuilder.newBuilder()
            .maximumSize(32)
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .build();

    private final Map<DataTileKey<?>, TileFuture<?>> queuedTiles = new HashMap<>();

    public void enqueueData(Set<DataTileKey<?>> requiredData) {
        for (DataTileKey<?> key : requiredData) {
            if (!this.queuedTiles.containsKey(key)) {
                this.enqueueTile(key);
            }
        }
        this.collectCompletedTiles();
    }

    private <T extends TiledDataAccess> TileFuture<T> enqueueTile(DataTileKey<T> key) {
        TileFuture<T> future = new TileFuture<>(key);
        future.submitTo(this.loadService);
        this.queuedTiles.put(key, future);
        return future;
    }

    @SuppressWarnings("unchecked")
    public <T extends TiledDataAccess> T getTile(TiledDataSource<T> source, DataTilePos pos) {
        this.collectCompletedTiles();

        DataTileKey<T> key = new DataTileKey<>(source, pos.getTileX(), pos.getTileZ());
        try {
            T result = (T) this.tileCache.getIfPresent(key);
            if (result != null) {
                return result;
            }

            TileFuture<T> tileFuture = (TileFuture<T>) this.queuedTiles.get(key);
            if (tileFuture == null) {
                tileFuture = this.enqueueTile(key);
            }

            T loadedResult = this.parseResult(tileFuture);
            this.queuedTiles.remove(key);
            this.tileCache.put(key, loadedResult);

            return loadedResult;
        } catch (Exception e) {
            Terrarium.LOGGER.warn("Unexpected exception occurred at {} from {}", pos, source.getIdentifier(), e);
            LoadingStateHandler.countFailure();
        }

        return source.getDefaultTile();
    }

    private void collectCompletedTiles() {
        if (this.queuedTiles.isEmpty()) {
            return;
        }

        Set<DataTileKey<?>> completedTiles = new HashSet<>();
        for (TileFuture<?> future : this.queuedTiles.values()) {
            if (future.isComplete()) {
                TiledDataAccess parsedResult = this.parseResult(future);
                this.tileCache.put(future.key, parsedResult);
                completedTiles.add(future.key);
            }
        }

        completedTiles.forEach(this.queuedTiles::remove);

        System.out.println(this.tileCache.size());
    }

    private <T extends TiledDataAccess> T parseResult(TileFuture<T> future) {
        DataTileKey<T> key = future.key;
        SourceResult<T> result = future.getResult();
        if (result.isError()) {
            Terrarium.LOGGER.warn("Loading tile at {} from {} gave error {}: {}", key.toPos(), key.getSource().getIdentifier(), result.getError(), result.getErrorCause());
            LoadingStateHandler.countFailure();
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
        DataTilePos finalPos = source.getFinalTilePos(key.toPos());
        if (finalPos == null) {
            return SourceResult.empty();
        }
        File cachedFile = new File(source.getCacheRoot(), source.getCachedName(finalPos));
        if (!source.shouldLoadCache(finalPos, cachedFile)) {
            return this.loadRemoteTile(source, finalPos, cachedFile);
        } else {
            return this.loadCachedTile(source, finalPos, cachedFile);
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

        public SourceResult<T> getResult() {
            try {
                if (this.future == null) {
                    return SourceResult.empty();
                }
                return this.future.get();
            } catch (ExecutionException e) {
                return SourceResult.exception(e);
            } catch (InterruptedException e) {
                return SourceResult.empty();
            }
        }

        public void submitTo(ExecutorService service) {
            if (!service.isShutdown()) {
                this.future = service.submit(() -> DataSourceHandler.this.loadTileRobustly(this.key));
            }
        }
    }
}
