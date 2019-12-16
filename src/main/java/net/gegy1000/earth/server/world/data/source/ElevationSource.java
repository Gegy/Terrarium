package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.Zoomed;
import net.gegy1000.earth.server.world.data.index.EarthRemoteIndex2;
import net.gegy1000.earth.server.world.data.source.cache.AbstractRegionKey;
import net.gegy1000.earth.server.world.data.source.cache.CachingInput;
import net.gegy1000.earth.server.world.data.source.cache.FileTileCache;
import net.gegy1000.earth.server.world.data.source.reader.TerrariumRasterReader;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.coordinate.ScaledCoordRef;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.data.source.DataTilePos;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

public class ElevationSource extends TiledDataSource<ShortRaster> {
    public static final int TILE_SIZE = 1200;
    private static final ShortRaster DEFAULT_RESULT = ShortRaster.createSquare(TILE_SIZE);

//    private static final TileCache<Key> CACHE = RegionTileCache.<Key>builder()
//            .keyProvider(new KeyProvider())
//            .inDirectory(GLOBAL_CACHE_ROOT.resolve("elevation"))
//            .sectorSize(512 * 1024)
//            .build();

    private final int zoom;

    private final Path cacheRoot;
    private final FileTileCache<DataTilePos> cache;
    private final CachingInput<DataTilePos> cachingInput;

    public ElevationSource(Zoomed<CoordinateReference> crs, int zoom) {
        super(crs.forZoom(zoom), TILE_SIZE, TILE_SIZE);

        this.zoom = zoom;
        this.cacheRoot = GLOBAL_CACHE_ROOT.resolve("elevation").resolve(String.valueOf(zoom));

        this.cache = new FileTileCache<>(pos -> this.cacheRoot.resolve(pos.getX() + "/" + pos.getZ()));
        this.cachingInput = new CachingInput<>(this.cache);
    }

    public static CoordinateReference crs(double worldScale, int zoom) {
        double globalWidth = globalWidth(zoom);
        return new ScaledCoordRef(worldScale * EarthWorld.EQUATOR_CIRCUMFERENCE / globalWidth);
    }

    public static double tileSizeDeg(int zoom) {
        return Math.pow(3.0, 2.0 - zoom);
    }

    public static double globalWidth(int zoom) {
        return (360.0 / tileSizeDeg(zoom)) * TILE_SIZE;
    }

    public static double estimateResolutionMeters(int zoom) {
        return 30.0 * Math.pow(3.0, 3.0 - zoom);
    }

    @Override
    public Optional<ShortRaster> load(DataTilePos pos) throws IOException {
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
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", TerrariumEarth.USER_AGENT);
            return connection.getInputStream();
        })) {
            return Optional.of(TerrariumRasterReader.read(input, ShortRaster.class));
        }
    }

    @Override
    public ShortRaster getDefaultResult() {
        return DEFAULT_RESULT;
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
