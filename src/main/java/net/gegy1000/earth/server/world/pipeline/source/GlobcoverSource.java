package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.pipeline.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceException;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class GlobcoverSource extends TiledDataSource<CoverRasterTile> implements CachedRemoteSource {
    public static final int TILE_SIZE = 2560;

    private final File cacheRoot;

    public GlobcoverSource(CoordinateState coordinateState, String cacheRoot) {
        super(new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE), 4);
        this.cacheRoot = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, cacheRoot);
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        URL url = new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getGlobEndpoint(), this.getCachedName(key)));
        return new GZIPInputStream(url.openStream());
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format(EarthRemoteData.info.getGlobQuery(), key.getTileX(), key.getTileZ());
    }

    @Override
    public CoverRasterTile loadTile(DataTilePos key) throws SourceException {
        try (DataInputStream input = new DataInputStream(this.getStream(key))) {
            int width = input.readUnsignedShort();
            int height = input.readUnsignedShort();

            int offsetX = key.getTileX() < 0 ? TILE_SIZE - width : 0;
            int offsetZ = key.getTileZ() < 0 ? TILE_SIZE - height : 0;

            byte[] buffer = new byte[width * height];
            input.readFully(buffer);

            CoverType<?>[] cover = new CoverType<?>[buffer.length];
            for (int i = 0; i < buffer.length; i++) {
                cover[i] = EarthCoverTypes.Glob.get(buffer[i]).getCoverType();
            }
            return new CoverRasterTile(cover, offsetX, offsetZ, width, height);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to parse heights tile at {}", key, e);
        }

        return null;
    }

    @Override
    public Class<CoverRasterTile> getTileType() {
        return CoverRasterTile.class;
    }

    @Override
    protected CoverRasterTile getDefaultTile() {
        CoverType<?>[] backingData = ArrayUtils.defaulted(new CoverType<?>[TILE_SIZE * TILE_SIZE], TerrariumCoverTypes.PLACEHOLDER);
        return new CoverRasterTile(backingData, 0, 0, TILE_SIZE, TILE_SIZE);
    }
}
