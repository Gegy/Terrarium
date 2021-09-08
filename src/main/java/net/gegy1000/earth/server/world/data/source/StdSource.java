package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.util.IoFunction;
import net.gegy1000.earth.server.util.zoom.ZoomLevels;
import net.gegy1000.earth.server.util.zoom.Zoomable;
import net.gegy1000.earth.server.world.data.source.cache.CachingInput;
import net.gegy1000.earth.server.world.data.source.cache.FileTileCache;
import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.source.TerrariumCacheDirs;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

public final class StdSource<T> extends TiledDataSource<T> {
    public static final int TILE_SIZE = 1000;
    private static final int ZOOM_BASE = 3;

    public static final String ENDPOINT = "https://terrarium.gegy.dev/geo3";

    private final IoFunction<InputStream, T> read;
    private final String endpoint;
    private final int zoom;

    private final CachingInput<Vec2i> cachingInput;

    private StdSource(
            String cacheName,
            String endpoint,
            IoFunction<InputStream, T> read,
            int zoom
    ) {
        super(TILE_SIZE);

        this.read = read;
        this.endpoint = ENDPOINT + "/" + endpoint + "/" + zoom;
        this.zoom = zoom;

        Path cacheRoot = TerrariumCacheDirs.GLOBAL_ROOT.resolve(cacheName + "/" + zoom);
        FileTileCache<Vec2i> cache = new FileTileCache<>(pos -> cacheRoot.resolve(pos.x + "/" + pos.y));
        this.cachingInput = new CachingInput<>(cache);
    }

    public static <T> Builder<T> builder(ZoomLevels zoomLevels) {
        return new Builder<>(zoomLevels);
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
        return Math.log(EarthWorld.EQUATOR_CIRCUMFERENCE / (2 * TILE_SIZE * meters)) / Math.log(ZOOM_BASE);
    }

    public static boolean containsTile(Vec2i pos, int zoom) {
        int tileCountX = MathHelper.ceil(tileCountX(zoom));
        int tileCountY = MathHelper.ceil(tileCountY(zoom));
        return pos.x >= 0 && pos.y >= 0 && pos.x < tileCountX && pos.y < tileCountY;
    }

    @Override
    public Optional<T> load(Vec2i pos) throws IOException {
        if (!StdSource.containsTile(pos, this.zoom)) {
            return Optional.empty();
        }

        String url = this.endpoint + "/" + pos.x + "/" + pos.y;
        try (InputStream input = this.cachingInput.getInputStream(pos, p -> httpGet(new URL(url)))) {
            return Optional.of(this.read.apply(input));
        }
    }

    public static class Builder<T> {
        private final ZoomLevels zoomLevels;

        private String cacheName;
        private IoFunction<InputStream, T> read;
        private String endpoint;

        private Builder(ZoomLevels zoomLevels) {
            this.zoomLevels = zoomLevels;
        }

        public Builder<T> cacheName(String cacheName) {
            this.cacheName = cacheName;
            return this;
        }

        public Builder<T> endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder<T> read(IoFunction<InputStream, T> read) {
            this.read = read;
            return this;
        }

        public Zoomable<StdSource<T>> build() {
            return this.zoomLevels.map(zoom -> new StdSource<>(this.cacheName, this.endpoint, this.read, zoom));
        }
    }
}
