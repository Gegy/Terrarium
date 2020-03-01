package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.world.data.index.EarthRemoteIndex2;
import net.gegy1000.earth.server.world.data.source.cache.AbstractRegionKey;
import net.gegy1000.earth.server.world.data.source.cache.CachingInput;
import net.gegy1000.earth.server.world.data.source.cache.FileTileCache;
import net.gegy1000.earth.server.world.data.source.reader.TerrariumRasterReader;
import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

public class ElevationSource extends TiledDataSource<ShortRaster> {
    public static final int TILE_SIZE = 1200;

//    private static final TileCache<Key> CACHE = RegionTileCache.<Key>builder()
//            .keyProvider(new KeyProvider())
//            .inDirectory(GLOBAL_CACHE_ROOT.resolve("elevation"))
//            .sectorSize(512 * 1024)
//            .build();

    private final int zoom;

    private final CachingInput<Vec2i> cachingInput;

    public ElevationSource(int zoom, CoordinateReference crs) {
        super(crs, TILE_SIZE);

        this.zoom = zoom;

        Path cacheRoot = GLOBAL_CACHE_ROOT.resolve("elevation").resolve(String.valueOf(zoom));
        FileTileCache<Vec2i> cache = new FileTileCache<>(pos -> cacheRoot.resolve(pos.x + "/" + pos.y));
        this.cachingInput = new CachingInput<>(cache);
    }

    public static ZoomLevels zoomLevels() {
        return ZoomLevels.range(0, 3);
    }

    public static CoordinateReference crs(double worldScale, int zoom) {
        double globalWidth = globalWidth(zoom);
        return CoordinateReference.scale(EarthWorld.EQUATOR_CIRCUMFERENCE / (globalWidth * worldScale));
    }

    public static double tileSizeDeg(int zoom) {
        return Math.pow(3.0, 2.0 - zoom);
    }

    public static double globalWidth(int zoom) {
        return (360.0 / tileSizeDeg(zoom)) * TILE_SIZE;
    }

    public static double resolutionMeters(int zoom) {
        return 30.0 * Math.pow(3.0, 3.0 - zoom);
    }

    @Override
    public Optional<ShortRaster> load(Vec2i pos) throws IOException {
        SharedEarthData sharedData = SharedEarthData.instance();
        EarthRemoteIndex2 remoteIndex = sharedData.get(SharedEarthData.REMOTE_INDEX2);
        if (remoteIndex == null) {
            return Optional.empty();
        }

        String url = remoteIndex.elevation.forZoom(this.zoom).getUrlFor(pos);
        if (url == null) {
            return Optional.empty();
        }

        try (InputStream input = this.cachingInput.getInputStream(pos, p -> {
            HttpResponse response = HTTP.execute(new HttpGet(url));
            return response.getEntity().getContent();
        })) {
            return Optional.of(TerrariumRasterReader.read(input, ShortRaster.class));
        }
    }

    private static final int LOC_BITS = 3;

    private static class Key extends AbstractRegionKey<Key> {
        Key(int x, int z) {
            super(x, z);
        }

        @Override
        protected int bits() {
            return LOC_BITS;
        }
    }

    private static class KeyProvider extends AbstractRegionKey.Provider<Key> {
        @Override
        protected Key create(int x, int z) {
            return new Key(x, z);
        }

        @Override
        protected int bits() {
            return LOC_BITS;
        }
    }
}
