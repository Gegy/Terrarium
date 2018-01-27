package net.gegy1000.terrarium.server.map.source.height;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.map.source.SourceException;
import net.gegy1000.terrarium.server.map.source.TerrariumData;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class HeightSource extends TiledSource<HeightTileAccess> implements CachedRemoteSource {
    public static final int TILE_SIZE = 1201;

    private static final File CACHE_ROOT = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, "heights");
    private static final Set<DataTilePos> VALID_TILES = new HashSet<>();

    private final EarthGenerationSettings settings;

    public HeightSource(EarthGenerationSettings settings) {
        super(1200, 9);
        this.settings = settings;
    }

    public static void loadValidTiles() {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(HeightSource.getTilesURL().openStream())))) {
            int count = input.readInt();
            for (int i = 0; i < count; i++) {
                int latitude = input.readShort();
                int longitude = input.readShort();
                VALID_TILES.add(new DataTilePos(longitude, latitude));
            }
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to load valid height tiles", e);
        }
    }

    private static URL getTilesURL() throws IOException {
        return new URL(String.format("%s/%s/%s", TerrariumData.info.getBaseURL(), TerrariumData.info.getHeightsEndpoint(), TerrariumData.info.getHeightTiles()));
    }

    @Override
    public HeightTileAccess loadTile(DataTilePos key) throws SourceException {
        if (VALID_TILES.isEmpty() || VALID_TILES.contains(key)) {
            try (DataInputStream input = new DataInputStream(this.getStream(key))) {
                short[] heightmap = new short[TILE_SIZE * TILE_SIZE];
                for (int i = 0; i < heightmap.length; i++) {
                    heightmap[i] = input.readShort();
                }
                return new HeightTileAccess(heightmap, TILE_SIZE, TILE_SIZE);
            } catch (IOException e) {
                Terrarium.LOGGER.error("Failed to parse height tile at {}", key, e);
            }
        }
        return null;
    }

    @Override
    protected HeightTileAccess getDefaultTile() {
        return new HeightTileAccess(new short[TILE_SIZE * TILE_SIZE], TILE_SIZE, TILE_SIZE);
    }

    @Override
    public File getCacheRoot() {
        return CACHE_ROOT;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        String cachedName = this.getCachedName(key);
        URL url = new URL(String.format("%s/%s/%s", TerrariumData.info.getBaseURL(), TerrariumData.info.getHeightsEndpoint(), cachedName));
        return new GZIPInputStream(url.openStream());
    }

    @Override
    public String getCachedName(DataTilePos key) {
        String latitudePrefix = key.getTileY() < 0 ? "S" : "N";
        String longitudePrefix = key.getTileX() < 0 ? "W" : "E";

        StringBuilder latitudeString = new StringBuilder(String.valueOf(Math.abs(key.getTileY())));
        while (latitudeString.length() < 2) {
            latitudeString.insert(0, "0");
        }
        latitudeString.insert(0, latitudePrefix);

        StringBuilder longitudeString = new StringBuilder(String.valueOf(Math.abs(key.getTileX())));
        while (longitudeString.length() < 3) {
            longitudeString.insert(0, "0");
        }
        longitudeString.insert(0, longitudePrefix);

        return String.format(TerrariumData.info.getHeightsQuery(), latitudeString.toString(), longitudeString.toString());
    }

    @Override
    public EarthGenerationSettings getSettings() {
        return this.settings;
    }
}
