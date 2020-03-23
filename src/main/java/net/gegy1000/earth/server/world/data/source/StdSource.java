package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.IoFunction;
import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.index.DataIndex3;
import net.gegy1000.earth.server.world.data.source.cache.CachingInput;
import net.gegy1000.earth.server.world.data.source.cache.FileTileCache;
import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

public final class StdSource<T> extends TiledDataSource<T> {
    public static final int TILE_SIZE = 1000;
    private static final int ZOOM_BASE = 3;

    private final IoFunction<InputStream, T> read;
    private final Function<DataIndex3, DataIndex3.Endpoint> endpoint;

    private final CachingInput<Vec2i> cachingInput;

    private StdSource(
            String cacheName,
            IoFunction<InputStream, T> read,
            Function<DataIndex3, DataIndex3.Endpoint> endpoint
    ) {
        super(TILE_SIZE);

        this.read = read;
        this.endpoint = endpoint;

        Path cacheRoot = GLOBAL_CACHE_ROOT.resolve(cacheName);
        FileTileCache<Vec2i> cache = new FileTileCache<>(pos -> cacheRoot.resolve(pos.x + "/" + pos.y));
        this.cachingInput = new CachingInput<>(cache);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> ZoomBuilder<T> builder(ZoomLevels zoomLevels) {
        return new ZoomBuilder<>(zoomLevels);
    }

    public static CoordinateReference crs(double worldScale, int zoom) {
        double globalWidth = globalWidth(zoom);
        double globalHeight = globalHeight(zoom);

        double scale = EarthWorld.EQUATOR_CIRCUMFERENCE / (worldScale * globalWidth);
        double offsetX = -globalWidth / 2.0;
        double offsetZ = -globalHeight / 2.0;

        return CoordinateReference.scaleAndOffset(scale, scale, offsetX, offsetZ);
    }

    public static double tileSizeDeg(int zoom) {
        return 360.0 / tileCountX(zoom);
    }

    public static double tileCountX(int zoom) {
        return tileCountY(zoom) * 2.0;
    }

    public static double tileCountY(int zoom) {
        return Math.pow(ZOOM_BASE, zoom);
    }

    public static double globalWidth(int zoom) {
        return tileCountX(zoom) * TILE_SIZE;
    }

    public static double globalHeight(int zoom) {
        return tileCountY(zoom) * TILE_SIZE;
    }

    public static double metersPerPixel(int zoom) {
        return EarthWorld.EQUATOR_CIRCUMFERENCE / globalWidth(zoom);
    }

    public static double zoomForScale(double meters) {
        return Math.log(EarthWorld.EQUATOR_CIRCUMFERENCE / (2 * TILE_SIZE * meters)) / Math.log(3);
    }

    @Override
    public Optional<T> load(Vec2i pos) throws IOException {
        DataIndex3 index = SharedEarthData.instance().get(SharedEarthData.REMOTE_INDEX3);
        if (index == null) {
            throw new IllegalStateException("remote index not initialized");
        }

        String url = this.endpoint.apply(index).getUrlFor(pos);
        if (url == null) {
            return Optional.empty();
        }

        try (InputStream input = this.cachingInput.getInputStream(pos, p -> get(new URL(url)))) {
            return Optional.of(this.read.apply(input));
        }
    }

    public static class Builder<T> {
        private String cacheName;
        private IoFunction<InputStream, T> read;
        private Function<DataIndex3, DataIndex3.Endpoint> endpoint;

        private Builder() {
        }

        public Builder<T> cacheName(String cacheName) {
            this.cacheName = cacheName;
            return this;
        }

        public Builder<T> read(IoFunction<InputStream, T> read) {
            this.read = read;
            return this;
        }

        public Builder<T> endpoint(Function<DataIndex3, DataIndex3.Endpoint> endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public StdSource<T> build() {
            return new StdSource<>(this.cacheName, this.read, this.endpoint);
        }
    }

    public static class ZoomBuilder<T> {
        private final ZoomLevels zoomLevels;

        private String cacheName;
        private IoFunction<InputStream, T> read;
        private Function<DataIndex3, Zoomable<DataIndex3.Endpoint>> endpoint;

        private ZoomBuilder(ZoomLevels zoomLevels) {
            this.zoomLevels = zoomLevels;
        }

        public ZoomBuilder<T> cacheName(String cacheName) {
            this.cacheName = cacheName;
            return this;
        }

        public ZoomBuilder<T> read(IoFunction<InputStream, T> read) {
            this.read = read;
            return this;
        }

        public ZoomBuilder<T> endpoint(Function<DataIndex3, Zoomable<DataIndex3.Endpoint>> endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Zoomable<StdSource<T>> build() {
            return this.zoomLevels.map(zoom -> {
                String cacheName = this.cacheName + "/" + zoom;
                return new StdSource<>(cacheName, this.read, idx -> this.endpoint.apply(idx).forZoom(zoom));
            });
        }
    }
}
