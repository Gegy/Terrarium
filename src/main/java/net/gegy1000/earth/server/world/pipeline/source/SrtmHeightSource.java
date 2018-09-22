package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceResult;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.util.ResourceLocation;
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

public class SrtmHeightSource extends TiledDataSource<ShortRasterTile> {
    public static final int TILE_SIZE = 1200;
    private static final ShortRasterTile DEFAULT_TILE = new ShortRasterTile(new short[TILE_SIZE * TILE_SIZE], TILE_SIZE, TILE_SIZE);

    private static final Set<DataTilePos> VALID_TILES = new HashSet<>();

    public SrtmHeightSource(CoordinateState coordinateState, String cacheRoot) {
        super(new ResourceLocation(TerrariumEarth.MODID, "srtm"), new File(GLOBAL_CACHE_ROOT, cacheRoot), new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE));
    }

    public static void loadValidTiles() throws IOException {
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new GZIPInputStream(SrtmHeightSource.getTilesURL().openStream())))) {
            int count = input.readInt();
            for (int i = 0; i < count; i++) {
                int latitude = input.readShort();
                int longitude = input.readShort();
                VALID_TILES.add(new DataTilePos(longitude, -latitude));
            }
        }
    }

    private static URL getTilesURL() throws IOException {
        return new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getHeightsEndpoint(), EarthRemoteData.info.getHeightTiles()));
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
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
    public ShortRasterTile getDefaultTile() {
        return DEFAULT_TILE;
    }

    @Nullable
    @Override
    public DataTilePos getFinalTilePos(DataTilePos pos) {
        if (!VALID_TILES.isEmpty() && !VALID_TILES.contains(pos)) {
            return null;
        }
        return new DataTilePos(pos.getTileX(), pos.getTileZ() + 1);
    }

    @Override
    public SourceResult<ShortRasterTile> parseStream(DataTilePos pos, InputStream stream) throws IOException {
        try (DataInputStream input = new DataInputStream(stream)) {
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
            return SourceResult.success(new ShortRasterTile(heightmap, TILE_SIZE, TILE_SIZE));
        }
    }
}
