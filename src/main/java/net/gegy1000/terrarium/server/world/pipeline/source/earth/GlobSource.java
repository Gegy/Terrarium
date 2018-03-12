package net.gegy1000.terrarium.server.world.pipeline.source.earth;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceException;
import net.gegy1000.terrarium.server.world.pipeline.source.TerrariumRemoteData;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.minecraft.util.JsonUtils;
import net.minecraft.world.World;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class GlobSource extends TiledDataSource<CoverRasterTileAccess> implements CachedRemoteSource {
    public static final int TILE_SIZE = 2560;

    private final File cacheRoot;

    public GlobSource(CoordinateState coordinateState, String cacheRoot) {
        super(new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE), 4);
        this.cacheRoot = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, cacheRoot);
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        URL url = new URL(String.format("%s/%s/%s", TerrariumRemoteData.info.getBaseURL(), TerrariumRemoteData.info.getGlobEndpoint(), this.getCachedName(key)));
        return new GZIPInputStream(url.openStream());
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format(TerrariumRemoteData.info.getGlobQuery(), key.getTileX(), key.getTileZ());
    }

    @Override
    public CoverRasterTileAccess loadTile(DataTilePos key) throws SourceException {
        try (DataInputStream input = new DataInputStream(this.getStream(key))) {
            int width = input.readUnsignedShort();
            int height = input.readUnsignedShort();

            int offsetX = key.getTileX() < 0 ? TILE_SIZE - width : 0;
            int offsetZ = key.getTileZ() < 0 ? TILE_SIZE - height : 0;

            byte[] buffer = new byte[width * height];
            input.readFully(buffer);

            return CoverRasterTileAccess.loadGlob(buffer, offsetX, offsetZ, width, height);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to parse heights tile at {}", key, e);
        }

        return null;
    }

    @Override
    public Class<CoverRasterTileAccess> getTileType() {
        return CoverRasterTileAccess.class;
    }

    @Override
    protected CoverRasterTileAccess getDefaultTile() {
        CoverType[] backingData = ArrayUtils.defaulted(new CoverType[TILE_SIZE * TILE_SIZE], CoverType.NO_DATA);
        return new CoverRasterTileAccess(backingData, 0, 0, TILE_SIZE, TILE_SIZE);
    }

    public static class Parser implements InstanceObjectParser<TiledDataSource<?>> {
        @Override
        public TiledDataSource<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            CoordinateState coordinateState = valueParser.parseCoordinateState(objectRoot, "tile_coordinate");
            String cache = JsonUtils.getString(objectRoot, "cache");
            return new GlobSource(coordinateState, cache);
        }
    }
}
