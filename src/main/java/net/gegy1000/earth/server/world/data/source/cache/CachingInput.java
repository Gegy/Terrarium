package net.gegy1000.earth.server.world.data.source.cache;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class CachingInput<T> {
    private static final ExecutorService CACHE_SERVICE = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("terrarium-cache-service")
                    .setDaemon(true)
                    .build()
    );

    private final TileCache<T> cache;

    public CachingInput(TileCache<T> cache) {
        this.cache = cache;
    }

    public InputStream getInputStream(T key, IoFunction<T, InputStream> remoteFunction) throws IOException {
        InputStream localStream = this.cache.in(key);
        if (localStream != null) return localStream;

        InputStream remoteStream = remoteFunction.apply(key);
        OutputStream cacheStream = this.cache.out(key);

        if (cacheStream == null) return remoteStream;

        return getCachingStream(remoteStream, () -> cacheStream, e -> this.deleteQuietly(key));
    }

    public static InputStream getCachingStream(InputStream source, IoSupplier<OutputStream> destination, Consumer<IOException> error) throws IOException {
        PipedOutputStream sink = new PipedOutputStream();
        InputStream input = new PipedInputStream(sink);
        CACHE_SERVICE.submit(() -> {
            try {
                try (OutputStream out = destination.get()) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = source.read(buffer)) != IOUtils.EOF) {
                        out.write(buffer, 0, count);
                        sink.write(buffer, 0, count);
                    }
                }
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to read or cache remote data", e);
                error.accept(e);
            } finally {
                IOUtils.closeQuietly(source);
                IOUtils.closeQuietly(sink);
            }
        });
        return input;
    }

    private void deleteQuietly(T key) {
        try {
            this.cache.delete(key);
        } catch (IOException ignored) {
        }
    }

    public interface IoFunction<T, R> {
        R apply(T key) throws IOException;
    }

    public interface IoSupplier<T> {
        T get() throws IOException;
    }
}