package net.gegy1000.terrarium.server.world.pipeline.source;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface CachedRemoteSource {
    File GLOBAL_CACHE_ROOT = new File(".", "mods/terrarium/cache/");

    ExecutorService CACHE_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("terrarium-cache-service").setDaemon(true).build());

    File getCacheRoot();

    InputStream getRemoteStream(DataTilePos key) throws IOException;

    InputStream getWrappedStream(InputStream stream) throws IOException;

    String getCachedName(DataTilePos key);

    default <T> T parseStream(DataTilePos key, StreamParser<T> handler) throws NoDataException {
        File cacheRoot = this.getCacheRoot();
        if (!cacheRoot.exists()) {
            cacheRoot.mkdirs();
        }
        File cachedFile = new File(cacheRoot, this.getCachedName(key));
        if (!this.shouldLoadCache(key, cachedFile)) {
            LoadingStateHandler.StateEntry onlineEntry = LoadingStateHandler.makeState(LoadingState.LOADING_ONLINE);
            try (InputStream remoteStream = this.getRemoteStream(key)) {
                InputStream cachingStream = this.getCachingStream(remoteStream, cachedFile);
                this.cacheMetadata(key);
                return handler.parse(cachingStream);
            } catch (IOException e) {
                LoadingStateHandler.putState(LoadingState.LOADING_NO_CONNECTION);
                throw new NoDataException("Failed to load remote tile data stream at " + key, e);
            } finally {
                LoadingStateHandler.breakState(onlineEntry);
            }
        }

        LoadingStateHandler.StateEntry cachedEntry = LoadingStateHandler.makeState(LoadingState.LOADING_CACHED);
        try {
            return handler.parse(this.getWrappedStream(new BufferedInputStream(new FileInputStream(cachedFile))));
        } catch (IOException e) {
            LoadingStateHandler.putState(LoadingState.LOADING_NO_CONNECTION);
            Terrarium.LOGGER.error("Failed to load local tile data stream at {}", key, e);
            throw new NoDataException("Loading cache failed", e);
        } finally {
            LoadingStateHandler.breakState(cachedEntry);
        }
    }

    default InputStream getCachingStream(InputStream source, File cacheFile) throws IOException {
        PipedOutputStream sink = new PipedOutputStream();
        InputStream input = new PipedInputStream(sink);
        CACHE_SERVICE.submit(() -> {
            try (OutputStream file = new FileOutputStream(cacheFile)) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = source.read(buffer)) != IOUtils.EOF) {
                    file.write(buffer, 0, count);
                    sink.write(buffer, 0, count);
                }
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to read or cache remote data", e);
            } finally {
                IOUtils.closeQuietly(sink);
            }
        });
        return this.getWrappedStream(input);
    }

    default void cacheMetadata(DataTilePos key) {
    }

    default boolean shouldLoadCache(DataTilePos key, File file) {
        return file.exists();
    }

    default void removeCache(DataTilePos key) {
        File cachedFile = new File(this.getCacheRoot(), this.getCachedName(key));
        cachedFile.delete();
    }

    interface StreamParser<T> {
        T parse(InputStream stream) throws IOException;
    }
}
