package net.gegy1000.terrarium.server.world.data.source;

import net.gegy1000.terrarium.server.util.Vec2i;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class TiledDataSource<T> {
    public static final Path LEGACY_CACHE_ROOT = Paths.get(".", "mods/terrarium/cache");
    public static final Path GLOBAL_CACHE_ROOT = Paths.get(".", "mods/terrarium/cache2");

    public static final HttpClient HTTP = HttpClientBuilder.create()
            .setMaxConnTotal(8)
            .setUserAgent("terrarium")
            .build();

    protected final double tileWidth;
    protected final double tileHeight;

    protected TiledDataSource(double tileWidth, double tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    protected TiledDataSource(double tileSize) {
        this(tileSize, tileSize);
    }

    public final double getTileWidth() {
        return this.tileWidth;
    }

    public final double getTileHeight() {
        return this.tileHeight;
    }

    public abstract Optional<T> load(Vec2i pos) throws IOException;
}
