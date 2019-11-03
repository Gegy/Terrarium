package net.gegy1000.earth.server.world.data.source;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.Terrarium;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public final class CachingInput<T> {
    private static final ExecutorService CACHE_SERVICE = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("terrarium-cache-service")
                    .setDaemon(true)
                    .build()
    );

    private final Function<T, Path> pathFunction;

    public CachingInput(Function<T, Path> pathFunction) {
        this.pathFunction = pathFunction;
    }

    private Path getCachePath(T key) {
        if (this.pathFunction != null) {
            return this.pathFunction.apply(key);
        }
        return null;
    }

    public InputStream getInputStream(T key, RemoteFunction<T> remoteFunction) throws IOException {
        Path cachePath = this.getCachePath(key);
        if (cachePath != null && Files.exists(cachePath)) {
            return Files.newInputStream(cachePath);
        }

        InputStream remoteStream = remoteFunction.apply(key);
        if (cachePath != null) {
            return getCachingStream(remoteStream, cachePath);
        } else {
            return remoteStream;
        }
    }

    public static InputStream getCachingStream(InputStream source, Path cachePath) throws IOException {
        PipedOutputStream sink = new PipedOutputStream();
        InputStream input = new PipedInputStream(sink);
        CACHE_SERVICE.submit(() -> {
            try {
                Files.createDirectories(cachePath.getParent());
                try (OutputStream file = Files.newOutputStream(cachePath)) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = source.read(buffer)) != IOUtils.EOF) {
                        file.write(buffer, 0, count);
                        sink.write(buffer, 0, count);
                    }
                }
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to read or cache remote data", e);
                deleteQuietly(cachePath);
            } finally {
                IOUtils.closeQuietly(source);
                IOUtils.closeQuietly(sink);
            }
        });
        return input;
    }

    private static void deleteQuietly(Path cachePath) {
        try {
            Files.deleteIfExists(cachePath);
        } catch (IOException ignored) {
        }
    }

    public interface RemoteFunction<T> {
        InputStream apply(T key) throws IOException;
    }
}
