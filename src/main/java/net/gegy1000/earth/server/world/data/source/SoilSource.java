package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.server.capability.EarthWorld;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.index.EarthRemoteIndex2;
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
import java.util.function.Function;

public class SoilSource extends TiledDataSource<ShortRaster> {
    public static final int TILE_SIZE = 480;

    private final int zoom;
    private final CachingInput<Vec2i> cachingInput;

    private final Function<EarthRemoteIndex2, Zoomable<EarthRemoteIndex2.Endpoint>> endpointFunction;

    private SoilSource(int zoom, CoordinateReference crs, String cacheName, Function<EarthRemoteIndex2, Zoomable<EarthRemoteIndex2.Endpoint>> endpointFunction) {
        super(crs, TILE_SIZE);

        this.zoom = zoom;
        this.endpointFunction = endpointFunction;

        Path cacheRoot = GLOBAL_CACHE_ROOT.resolve(cacheName).resolve(String.valueOf(zoom));
        FileTileCache<Vec2i> cache = new FileTileCache<>(pos -> cacheRoot.resolve(pos.x + "/" + pos.y));
        this.cachingInput = new CachingInput<>(cache);
    }

    public static ZoomLevels zoomLevels() {
        return ZoomLevels.range(0, 1);
    }

    public static SoilSource cationExchangeCapacity(int zoom, CoordinateReference crs) {
        return new SoilSource(zoom, crs, "soil/cec", index -> index.cationExchangeCapacity);
    }

    public static SoilSource organicCarbonContent(int zoom, CoordinateReference crs) {
        return new SoilSource(zoom, crs, "soil/occ", index -> index.organicCarbonContent);
    }

    public static SoilSource ph(int zoom, CoordinateReference crs) {
        return new SoilSource(zoom, crs, "soil/ph", index -> index.ph);
    }

    public static SoilSource clayContent(int zoom, CoordinateReference crs) {
        return new SoilSource(zoom, crs, "soil/clay", index -> index.clayContent);
    }

    public static SoilSource siltContent(int zoom, CoordinateReference crs) {
        return new SoilSource(zoom, crs, "soil/silt", index -> index.siltContent);
    }

    public static SoilSource sandContent(int zoom, CoordinateReference crs) {
        return new SoilSource(zoom, crs, "soil/sand", index -> index.sandContent);
    }

    public static CoordinateReference crs(double worldScale, int zoom) {
        double globalWidth = globalWidth(zoom);
        return CoordinateReference.scale(EarthWorld.EQUATOR_CIRCUMFERENCE / (globalWidth * worldScale));
    }

    public static double tileSizeDeg(int zoom) {
        return Math.pow(3.0, 1.0 - zoom);
    }

    public static double globalWidth(int zoom) {
        return (360.0 / tileSizeDeg(zoom)) * TILE_SIZE;
    }

    @Override
    public Optional<ShortRaster> load(Vec2i pos) throws IOException {
        SharedEarthData sharedData = SharedEarthData.instance();
        EarthRemoteIndex2 remoteIndex = sharedData.get(SharedEarthData.REMOTE_INDEX2);
        if (remoteIndex == null) {
            return Optional.empty();
        }

        String url = this.endpointFunction.apply(remoteIndex).forZoom(this.zoom).getUrlFor(pos);
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
}
