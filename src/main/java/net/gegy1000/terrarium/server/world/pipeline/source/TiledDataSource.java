package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class TiledDataSource<T extends TiledDataAccess> {
    public static final File GLOBAL_CACHE_ROOT = new File(".", "mods/terrarium/cache/");

    protected final ResourceLocation identifier;
    protected final File cacheRoot;
    protected final Coordinate tileSize;

    protected TiledDataSource(ResourceLocation identifier, File cacheRoot, Coordinate tileSize) {
        this.identifier = identifier;
        this.cacheRoot = cacheRoot;
        this.tileSize = tileSize;
        if (!cacheRoot.exists()) {
            cacheRoot.mkdirs();
        }
    }

    public ResourceLocation getIdentifier() {
        return this.identifier;
    }

    public File getCacheRoot() {
        return this.cacheRoot;
    }

    public Coordinate getTileSize() {
        return this.tileSize;
    }

    public abstract InputStream getRemoteStream(DataTilePos key) throws IOException;

    public abstract InputStream getWrappedStream(InputStream stream) throws IOException;

    public abstract String getCachedName(DataTilePos key);

    public abstract T getDefaultTile();

    public abstract SourceResult<T> parseStream(DataTilePos pos, InputStream stream) throws IOException;

    @Nullable
    public T getLocalTile(DataTilePos pos) {
        return null;
    }

    public DataTilePos getLoadTilePos(DataTilePos pos) {
        return pos;
    }

    public void cacheMetadata(DataTilePos key) {
    }

    public boolean shouldLoadCache(DataTilePos key, File file) {
        return file.exists();
    }

    @Override
    public String toString() {
        return "TiledDataSource{identifier=" + this.identifier + "}";
    }
}
