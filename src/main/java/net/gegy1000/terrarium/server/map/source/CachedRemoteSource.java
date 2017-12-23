package net.gegy1000.terrarium.server.map.source;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import org.apache.commons.io.IOUtils;

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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public interface CachedRemoteSource {
    File GLOBAL_CACHE_ROOT = TerrariumData.CACHE_ROOT;
    ExecutorService CACHE_SERVICE = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Terrarium Cache Service").setDaemon(true).build());

    File getCacheRoot();

    InputStream getRemoteStream(DataTilePos key) throws IOException;

    String getCachedName(DataTilePos key);

    default InputStream getStream(DataTilePos key) {
        File cachedFile = new File(this.getCacheRoot(), this.getCachedName(key));
        if (!this.shouldLoadCache(key, cachedFile)) {
            try (InputStream remoteStream = this.getRemoteStream(key)) {
                LoadingStateHandler.putState(LoadingState.LOADING_ONLINE);
                byte[] remoteData = IOUtils.toByteArray(remoteStream);
                this.cacheData(key, cachedFile, remoteData);
                return new ByteArrayInputStream(remoteData);
            } catch (IOException e) {
                LoadingStateHandler.putState(LoadingState.LOADING_NO_CONNECTION);
                Terrarium.LOGGER.error("Failed to load remote tile data stream at {}", key, e);
            }
        }

        LoadingStateHandler.putState(LoadingState.LOADING_CACHED);
        try {
            return new BufferedInputStream(new GZIPInputStream(new FileInputStream(cachedFile)));
        } catch (IOException e) {
            LoadingStateHandler.putState(LoadingState.LOADING_NO_CONNECTION);
            Terrarium.LOGGER.info("Failed to load local tile data stream at {}", key, e);
        }

        return new ByteArrayInputStream(new byte[0]);
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
            try (OutputStream output = new GZIPOutputStream(new FileOutputStream(file))) {
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
