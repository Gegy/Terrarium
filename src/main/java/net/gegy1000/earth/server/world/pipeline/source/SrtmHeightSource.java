package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceException;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import org.tukaani.xz.SingleXZInputStream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class SrtmHeightSource extends TiledDataSource<ShortRasterTile> implements CachedRemoteSource {
    public static final int TILE_SIZE = 1200;

    private static final Set<DataTilePos> VALID_TILES = new HashSet<>();

    private final File cacheRoot;

    public SrtmHeightSource(CoordinateState coordinateState, String cacheRoot) {
        super(new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE), 9);
        this.cacheRoot = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, cacheRoot);
    }

    public static void loadValidTiles() {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(SrtmHeightSource.getTilesURL().openStream())))) {
            int count = input.readInt();
            for (int i = 0; i < count; i++) {
                int latitude = input.readShort();
                int longitude = input.readShort();
                VALID_TILES.add(new DataTilePos(longitude, -latitude));
            }
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to load valid height tiles", e);
        }
    }

    private static URL getTilesURL() throws IOException {
        return new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getHeightsEndpoint(), EarthRemoteData.info.getHeightTiles()));
    }

    @Override
    public ShortRasterTile loadTile(DataTilePos key) throws SourceException {
        key = new DataTilePos(key.getTileX(), key.getTileZ() + 1);
        if (VALID_TILES.isEmpty() || VALID_TILES.contains(key)) {
            try (DataInputStream input = new DataInputStream(this.getStream(key))) {
                short[] heightmap = new short[TILE_SIZE * TILE_SIZE];
                short origin = input.readShort();
                if (origin == -1) {
                    for (int i = 0; i < heightmap.length; i++) {
                        heightmap[i] = input.readShort();
                    }
                } else {
                    for (int i = 0; i < heightmap.length; i++) {
                        heightmap[i] = (short) ((input.readByte() & 0xFF) + origin);
                    }
                }
                return new ShortRasterTile(heightmap, TILE_SIZE, TILE_SIZE);
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to parse height tile at {} ({})", key, this.getCachedName(key), e);
            }
        }
        return null;
    }

    @Override
    public Class<ShortRasterTile> getTileType() {
        return ShortRasterTile.class;
    }

    @Override
    protected ShortRasterTile getDefaultTile() {
        return new ShortRasterTile(new short[TILE_SIZE * TILE_SIZE], TILE_SIZE, TILE_SIZE);
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        String cachedName = this.getCachedName(key);
        URL url = new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getHeightsEndpoint(), cachedName));
        return new SingleXZInputStream(url.openStream());
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
}
