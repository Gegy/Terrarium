package net.gegy1000.terrarium.server.world.pipeline.source;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import org.apache.commons.io.IOUtils;
import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.SingleXZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface CachedRemoteSource {
    File GLOBAL_CACHE_ROOT = new File(".", "mods/terrarium/cache/");

    ExecutorService CACHE_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Terrarium Cache Service").setDaemon(true).build());

    File getCacheRoot();

    InputStream getRemoteStream(DataTilePos key) throws IOException;

    String getCachedName(DataTilePos key);

    default InputStream getStream(DataTilePos key) throws NoDataException {
        File cacheRoot = this.getCacheRoot();
        if (!cacheRoot.exists()) {
            cacheRoot.mkdirs();
        }
        File cachedFile = new File(cacheRoot, this.getCachedName(key));
        if (!this.shouldLoadCache(key, cachedFile)) {
            LoadingStateHandler.StateEntry onlineEntry = LoadingStateHandler.makeState(LoadingState.LOADING_ONLINE);
            try (InputStream remoteStream = this.getRemoteStream(key)) {
                byte[] remoteData = IOUtils.toByteArray(remoteStream);
                this.cacheData(key, cachedFile, remoteData);
                LoadingStateHandler.breakState(onlineEntry);
                return new ByteArrayInputStream(remoteData);
            } catch (IOException e) {
                LoadingStateHandler.breakState(onlineEntry);
                LoadingStateHandler.putState(LoadingState.LOADING_NO_CONNECTION);
                throw new NoDataException("Failed to load remote tile data stream at " + key, e);
            }
        }

        LoadingStateHandler.putState(LoadingState.LOADING_CACHED);
        try {
            return new BufferedInputStream(new SingleXZInputStream(new FileInputStream(cachedFile)));
        } catch (IOException e) {
            LoadingStateHandler.putState(LoadingState.LOADING_NO_CONNECTION);
            Terrarium.LOGGER.error("Failed to load local tile data stream at {}", key, e);
            throw new NoDataException("Loading cache failed", e);
        }
    }

    default void cacheMetadata(DataTilePos key) {
    }

    default boolean shouldLoadCache(DataTilePos key, File file) {
        return file.exists();
    }

    default void cacheData(DataTilePos key, File file, byte[] remoteData) {
        CACHE_SERVICE.submit(() -> {
            File cacheRoot = this.getCacheRoot();
            if (!cacheRoot.exists()) {
                cacheRoot.mkdirs();
            }
            // TODO: If we're taking in an XZ input, we don't need to compress it again when we output
            try (OutputStream output = new XZOutputStream(new FileOutputStream(file), new LZMA2Options())) {
                output.write(remoteData);
                this.cacheMetadata(key);
            } catch (Exception e) {
                Terrarium.LOGGER.error("Failed to cache tile at {} to {}", key, file, e);
            }
        });
    }

    default void removeCache(DataTilePos key) {
        File cachedFile = new File(this.getCacheRoot(), this.getCachedName(key));
        cachedFile.delete();
    }
}
