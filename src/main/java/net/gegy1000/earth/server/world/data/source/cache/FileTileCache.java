package net.gegy1000.earth.server.world.data.source.cache;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

public final class FileTileCache<T> implements TileCache<T> {
    private final Function<T, Path> pathFunction;

    public FileTileCache(Function<T, Path> pathFunction) {
        this.pathFunction = pathFunction;
    }

    private Path getCachePath(T key) {
        if (this.pathFunction != null) {
            return this.pathFunction.apply(key);
        }
        return null;
    }

    @Nullable
    @Override
    public InputStream in(T key) throws IOException {
        Path cachePath = this.getCachePath(key);
        if (cachePath != null && Files.exists(cachePath)) {
            return Files.newInputStream(cachePath);
        }
        return null;
    }

    @Nullable
    @Override
    public OutputStream out(T key) throws IOException {
        Path cachePath = this.getCachePath(key);
        if (cachePath == null) return null;

        Files.createDirectories(cachePath.getParent());
        return Files.newOutputStream(cachePath);
    }

    @Override
    public void delete(T key) throws IOException {
        Path cachePath = this.getCachePath(key);
        if (cachePath != null) {
            Files.deleteIfExists(cachePath);
        }
    }
}
