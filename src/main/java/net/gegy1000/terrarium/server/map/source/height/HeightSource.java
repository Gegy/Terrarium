package net.gegy1000.terrarium.server.map.source.height;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.map.source.SourceException;
import net.gegy1000.terrarium.server.map.source.TerrariumData;
import net.gegy1000.terrarium.server.map.source.raster.ShortRasterSource;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class HeightSource extends TiledSource<HeightTileAccess> implements ShortRasterSource, CachedRemoteSource {
    public static final int TILE_SIZE = 1201;

    private static final File CACHE_ROOT = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, "heights");
    private static final Set<DataTilePos> VALID_TILES = new HashSet<>();

    private final EarthGenerationSettings settings;

    public HeightSource(EarthGenerationSettings settings) {
        super(TILE_SIZE, 9);
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
    public short get(Coordinate coordinate) {
        DataTilePos pos = this.getTilePos(coordinate);
        HeightTileAccess tile = this.getTile(pos);
        return tile.get(MathHelper.floor(coordinate.getGlobalX() - this.getMinX(pos)), MathHelper.floor(coordinate.getGlobalZ() - this.getMinZ(pos)));
    }

    @Override
    public void sampleArea(short[] data, Coordinate minimumCoordinate, Coordinate maximumCoordinate) {
        Coordinate size = maximumCoordinate.subtract(minimumCoordinate);
        int width = MathHelper.ceil(size.getGlobalX());
        int height = MathHelper.ceil(size.getGlobalZ());
        if (data.length != width * height) {
            throw new IllegalArgumentException("Cannot sample to array of wrong size");
        }
        /*val minimumX = MathHelper.floor(minimumCoordinate.globalX)
        val minimumZ = MathHelper.floor(minimumCoordinate.globalZ)
        val maximumX = MathHelper.floor(maximumCoordinate.globalX)
        val maximumZ = MathHelper.floor(maximumCoordinate.globalZ)
        for (tileLatitude in MathHelper.floor(minimumCoordinate.latitude)..MathHelper.floor(maximumCoordinate.latitude)) {
            for (tileLongitude in MathHelper.floor(minimumCoordinate.longitude)..MathHelper.floor(maximumCoordinate.longitude)) {
                val pos = DataTilePos(tileLongitude, tileLatitude)
                val minX = pos.tileX * 1200
                val minZ = (-pos.tileY - 1) * 1200
                val tile = this.getTile(pos)
                val minSampleZ = Math.max(0, minimumZ - minZ)
                val maxSampleZ = Math.min(1200, maximumZ - minZ)
                val minSampleX = Math.max(0, minimumX - minX)
                val maxSampleX = Math.min(1200, maximumX - minX)
                for (z in minSampleZ..maxSampleZ - 1) {
                    val globalZ = z + minZ
                    val resultZ = globalZ - minimumZ
                    val resultIndexZ = resultZ * width
                    for (x in minSampleX..maxSampleX - 1) {
                        val globalX = x + minX
                        val resultX = globalX - minimumX
                        result[resultX + resultIndexZ] = tile.get(x, z)
                    }
                }
            }
        }*/
        // TODO: Come back to more efficient, but broken algorithm
        boolean tileEqualLat = MathHelper.floor(minimumCoordinate.getLatitude()) == MathHelper.floor(maximumCoordinate.getLatitude());
        boolean tileEqualLong = MathHelper.floor(minimumCoordinate.getLongitude()) == MathHelper.floor(maximumCoordinate.getLongitude());
        if (tileEqualLat && tileEqualLong) {
            DataTilePos pos = this.getTilePos(minimumCoordinate);
            HeightTileAccess tile = this.getTile(pos);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Coordinate coordinate = minimumCoordinate.add(x, y);
                    int tileX = MathHelper.floor(coordinate.getGlobalX() - this.getMinX(pos));
                    int tileZ = MathHelper.floor(coordinate.getGlobalZ() - this.getMinZ(pos));
                    short heightValue = tile.get(tileX, tileZ);
                    if (heightValue >= 0) {
                        data[x + y * width] = heightValue;
                    }
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    short heightValue = this.get(minimumCoordinate.add(x, y));
                    if (heightValue >= 0) {
                        data[x + y * width] = heightValue;
                    }
                }
            }
        }
    }

    @Override
    public EarthGenerationSettings getSettings() {
        return this.settings;
    }

    private int getMinX(DataTilePos pos) {
        return pos.getTileX() * 1200;
    }

    private int getMinZ(DataTilePos pos) {
        return (-pos.getTileY() - 1) * 1200;
    }

    private DataTilePos getTilePos(Coordinate coordinate) {
        return new DataTilePos(MathHelper.floor(coordinate.getLongitude()), MathHelper.floor(coordinate.getLatitude()));
    }
}
