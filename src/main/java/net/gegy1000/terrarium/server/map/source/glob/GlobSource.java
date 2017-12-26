package net.gegy1000.terrarium.server.map.source.glob;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.map.source.TerrariumData;
import net.gegy1000.terrarium.server.map.source.raster.RasterSource;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class GlobSource extends TiledSource<GlobTileAccess> implements RasterSource<GlobType>, CachedRemoteSource {
    public static final int TILE_SIZE = 2560;

    private static final File CACHE_ROOT = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, "globcover");

    private final EarthGenerationSettings settings;

    public GlobSource(EarthGenerationSettings settings) {
        super(TILE_SIZE, 4);
        this.settings = settings;
    }

    @Override
    public File getCacheRoot() {
        return CACHE_ROOT;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        URL url = new URL(String.format("%s/%s/%s", TerrariumData.info.getBaseURL(), TerrariumData.info.getGlobEndpoint(), this.getCachedName(key)));
        return new GZIPInputStream(url.openStream());
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format(TerrariumData.info.getGlobQuery(), key.getTileX(), key.getTileY());
    }

    @Override
    public GlobTileAccess loadTile(DataTilePos key) {
        try (DataInputStream input = new DataInputStream(this.getStream(key))) {
            int width = input.readUnsignedShort();
            int height = input.readUnsignedShort();

            int offsetX = key.getTileX() < 0 ? TILE_SIZE - width : 0;
            int offsetZ = key.getTileY() < 0 ? TILE_SIZE - height : 0;

            byte[] buffer = new byte[width * height];
            input.readFully(buffer);

            return new GlobTileAccess(buffer, offsetX, offsetZ, width, height);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to parse heights tile at {}", key, e);
        }

        return null;
    }

    @Override
    protected GlobTileAccess getDefaultTile() {
        return new GlobTileAccess(new byte[TILE_SIZE * TILE_SIZE], 0, 0, TILE_SIZE, TILE_SIZE);
    }

    @Override
    public EarthGenerationSettings getSettings() {
        return this.settings;
    }

    @Override
    public GlobType get(Coordinate coordinate) {
        double globX = coordinate.getGlobX();
        double globZ = coordinate.getGlobZ();

        int tileX = MathHelper.floor(globX / TILE_SIZE);
        int tileY = MathHelper.floor(globZ / TILE_SIZE);
        DataTilePos pos = new DataTilePos(tileX, tileY);
        GlobTileAccess tile = this.getTile(pos);

        return tile.get(MathHelper.floor(globX) - this.getMinX(pos), MathHelper.floor(globZ) - this.getMinZ(pos));
    }

    @Override
    public void sampleArea(GlobType[] data, Coordinate minimumCoordinate, Coordinate maximumCoordinate) {
        // TODO: Come back to more performant, but broken algorithm
        Coordinate size = maximumCoordinate.subtract(minimumCoordinate);
        int width = MathHelper.ceil(size.getGlobalX());
        int height = MathHelper.ceil(size.getGlobalZ());
        if (data.length != width * height) {
            throw new IllegalArgumentException("Cannot sample to array of wrong size");
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[x + y * width] = this.get(minimumCoordinate.add(x, y));
            }
        }
    }

    private int getMinX(DataTilePos pos) {
        return pos.getTileX() * TILE_SIZE;
    }

    private int getMinZ(DataTilePos pos) {
        return pos.getTileY() * TILE_SIZE;
    }
}
