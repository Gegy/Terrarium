package net.gegy1000.terrarium.server.world.data.source;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class TiledDataSource<T> {
    public static final Path LEGACY_CACHE_ROOT = Paths.get(".", "mods/terrarium/cache");
    public static final Path GLOBAL_CACHE_ROOT = Paths.get(".", "mods/terrarium/cache2");

    protected final Coordinate tileSize;

    protected TiledDataSource(Coordinate tileSize) {
        this.tileSize = tileSize;
    }

    public Coordinate getTileSize() {
        return this.tileSize;
    }

    public abstract Optional<T> load(DataTilePos pos) throws IOException;

    public abstract T getDefaultResult();
}
