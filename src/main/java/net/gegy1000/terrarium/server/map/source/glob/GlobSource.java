package net.gegy1000.terrarium.server.map.source.glob;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.map.source.SourceException;
import net.gegy1000.terrarium.server.map.source.TerrariumRemoteData;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class GlobSource extends TiledSource<CoverTileAccess> implements CachedRemoteSource {
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
        URL url = new URL(String.format("%s/%s/%s", TerrariumRemoteData.info.getBaseURL(), TerrariumRemoteData.info.getGlobEndpoint(), this.getCachedName(key)));
        return new GZIPInputStream(url.openStream());
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format(TerrariumRemoteData.info.getGlobQuery(), key.getTileX(), key.getTileY());
    }

    @Override
    public CoverTileAccess loadTile(DataTilePos key) throws SourceException {
        try (DataInputStream input = new DataInputStream(this.getStream(key))) {
            int width = input.readUnsignedShort();
            int height = input.readUnsignedShort();

            int offsetX = key.getTileX() < 0 ? TILE_SIZE - width : 0;
            int offsetZ = key.getTileY() < 0 ? TILE_SIZE - height : 0;

            byte[] buffer = new byte[width * height];
            input.readFully(buffer);

            return CoverTileAccess.loadGlob(buffer, offsetX, offsetZ, width, height);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to parse heights tile at {}", key, e);
        }

        return null;
    }

    @Override
    protected CoverTileAccess getDefaultTile() {
        CoverType[] backingData = ArrayUtils.defaulted(new CoverType[TILE_SIZE * TILE_SIZE], CoverType.NO_DATA);
        return new CoverTileAccess(backingData, 0, 0, TILE_SIZE, TILE_SIZE);
    }

    @Override
    public EarthGenerationSettings getSettings() {
        return this.settings;
    }
}
