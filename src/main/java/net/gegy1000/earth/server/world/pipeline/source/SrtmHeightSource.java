package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceResult;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import org.tukaani.xz.SingleXZInputStream;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class SrtmHeightSource extends TiledDataSource<ShortRaster> {
    public static final int TILE_SIZE = 1200;
    private static final ShortRaster DEFAULT_TILE = ShortRaster.createSquare(TILE_SIZE);

    private static final Set<DataTilePos> VALID_TILES = new HashSet<>();

    public SrtmHeightSource(CoordinateState coordinateState, String cacheRoot) {
        super(new File(GLOBAL_CACHE_ROOT, cacheRoot), new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE));
    }

    public static void loadValidTiles() throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(SrtmHeightSource.getTilesURL().openStream())))) {
            int count = input.readInt();
            for (int i = 0; i < count; i++) {
                int latitude = -input.readShort();
                int longitude = input.readShort();
                VALID_TILES.add(new DataTilePos(longitude, latitude));
            }
        }
    }

    private static URL getTilesURL() throws IOException {
        return new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getHeightsEndpoint(), EarthRemoteData.info.getHeightTiles()));
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        String cachedName = this.getCachedName(key);
        URL url = new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getHeightsEndpoint(), cachedName));
        return url.openStream();
    }

    @Override
    public InputStream getWrappedStream(InputStream stream) throws IOException {
        return new SingleXZInputStream(stream);
    }

    @Override
    public String getCachedName(DataTilePos key) {
        String latitudePrefix = -key.getTileZ() >= 0 ? "N" : "S";
        String longitudePrefix = key.getTileX() >= 0 ? "E" : "W";

        StringBuilder latitudeString = new StringBuilder(String.valueOf(Math.abs(key.getTileZ())));
        while (latitudeString.length() < 2) {
            latitudeString.insert(0, "0");
        }
        latitudeString.insert(0, latitudePrefix);

        StringBuilder longitudeString = new StringBuilder(String.valueOf(Math.abs(key.getTileX())));
        while (longitudeString.length() < 3) {
            longitudeString.insert(0, "0");
        }
        longitudeString.insert(0, longitudePrefix);

        return String.format(EarthRemoteData.info.getHeightsQuery(), latitudeString.toString(), longitudeString.toString());
    }

    @Override
    public ShortRaster getDefaultResult() {
        return DEFAULT_TILE;
    }

    @Override
    public DataTilePos getLoadTilePos(DataTilePos pos) {
        return new DataTilePos(pos.getTileX(), pos.getTileZ() + 1);
    }

    @Nullable
    @Override
    public ShortRaster getForcedTile(DataTilePos pos) {
        if (!VALID_TILES.isEmpty() && !VALID_TILES.contains(pos)) {
            return DEFAULT_TILE;
        }
        return null;
    }

    @Override
    public SourceResult<ShortRaster> parseStream(DataTilePos pos, InputStream stream) throws IOException {
        try (DataInputStream input = new DataInputStream(stream)) {
            ShortRaster heightmap = ShortRaster.createSquare(TILE_SIZE);

            short origin = input.readShort();
            if (origin == -1) {
                this.parseAbsolute(input, heightmap);
            } else {
                this.parseRelative(input, heightmap, origin);
            }

            return SourceResult.success(heightmap);
        }
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
