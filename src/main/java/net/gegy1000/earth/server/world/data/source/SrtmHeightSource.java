package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import org.tukaani.xz.SingleXZInputStream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

public class SrtmHeightSource extends TiledDataSource<ShortRaster> {
    private static final int TILE_SIZE = 1200;
    private static final ShortRaster DEFAULT_RESULT = ShortRaster.createSquare(TILE_SIZE);

    private static final Path CACHE_ROOT = GLOBAL_CACHE_ROOT.resolve("srtm_heights");

    private static final CachingInput<DataTilePos> CACHING_INPUT = new CachingInput<>(SrtmHeightSource::resolveCachePath);

    public SrtmHeightSource(CoordinateState coordinateState) {
        super(new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE));
    }

    private static Path resolveCachePath(DataTilePos pos) {
        int tileX = pos.getTileX();
        int tileZ = pos.getTileZ() + 1;

        String latitudePrefix = -tileZ >= 0 ? "N" : "S";
        String longitudePrefix = tileX >= 0 ? "E" : "W";

        StringBuilder latitudeString = new StringBuilder(String.valueOf(Math.abs(tileZ)));
        while (latitudeString.length() < 2) {
            latitudeString.insert(0, "0");
        }
        latitudeString.insert(0, latitudePrefix);

        StringBuilder longitudeString = new StringBuilder(String.valueOf(Math.abs(tileX)));
        while (longitudeString.length() < 3) {
            longitudeString.insert(0, "0");
        }
        longitudeString.insert(0, longitudePrefix);

        return CACHE_ROOT.resolve(latitudeString.toString() + longitudeString.toString() + ".ht2");
    }

    @Override
    public Optional<ShortRaster> load(DataTilePos pos) throws IOException {
        SharedEarthData sharedData = SharedEarthData.instance();
        EarthRemoteIndex remoteIndex = sharedData.get(SharedEarthData.REMOTE_INDEX);
        if (remoteIndex == null) {
            return Optional.empty();
        }

        String url = remoteIndex.srtm.getUrlFor(pos);
        if (url == null) {
            return Optional.empty();
        }

        InputStream sourceInput = CACHING_INPUT.getInputStream(pos, p -> {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", TerrariumEarth.USER_AGENT);
            return connection.getInputStream();
        });

        try (InputStream input = new SingleXZInputStream(new BufferedInputStream(sourceInput))) {
            return Optional.of(this.parseStream(input));
        }
    }

    @Override
    public ShortRaster getDefaultResult() {
        return DEFAULT_RESULT;
    }

    private ShortRaster parseStream(InputStream input) throws IOException {
        DataInputStream data = new DataInputStream(input);

        ShortRaster heightmap = ShortRaster.createSquare(TILE_SIZE);

        short origin = data.readShort();
        if (origin == -1) {
            this.parseAbsolute(data, heightmap);
        } else {
            this.parseRelative(data, heightmap, origin);
        }

        return heightmap;
    }

    private void parseRelative(DataInputStream input, ShortRaster heightmap, short origin) throws IOException {
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                short height = (short) ((input.readByte() & 0xFF) + origin);
                heightmap.set(x, y, height);
            }
        }
    }

    private void parseAbsolute(DataInputStream input, ShortRaster heightmap) throws IOException {
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                heightmap.set(x, y, input.readShort());
            }
        }
    }
}
