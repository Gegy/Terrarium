package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.CoverClassification;
import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.CoverRaster;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceResult;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraft.util.ResourceLocation;
import org.tukaani.xz.SingleXZInputStream;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class LandCoverSource extends TiledDataSource<CoverRaster> {
    public static final int TILE_SIZE = 1800;
    public static final int GLOBAL_WIDTH = 129600;
    public static final int GLOBAL_HEIGHT = 64800;

    private static final int TILE_COUNT_X = GLOBAL_WIDTH / TILE_SIZE;
    private static final int TILE_COUNT_Y = GLOBAL_HEIGHT / TILE_SIZE;

    private static final int TILE_OFFSET_X = TILE_COUNT_X / 2;
    private static final int TILE_OFFSET_Y = TILE_COUNT_Y / 2;

    private static final CoverRaster DEFAULT_TILE = new CoverRaster(TILE_SIZE, TILE_SIZE);

    static {
        CoverType[] data = DEFAULT_TILE.getData();
        Arrays.fill(data, EarthCoverTypes.SNOW);
    }

    public LandCoverSource(CoordinateState coordinateState, String cacheRoot) {
        super(new ResourceLocation(TerrariumEarth.MODID, "landcover"), new File(GLOBAL_CACHE_ROOT, cacheRoot), new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE));
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        URL url = new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getLandcoverEndpoint(), this.getCachedName(key)));
        return url.openStream();
    }

    @Override
    public InputStream getWrappedStream(InputStream stream) throws IOException {
        return new SingleXZInputStream(stream);
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format(EarthRemoteData.info.getLandcoverQuery(), key.getTileX(), key.getTileZ());
    }

    @Override
    public DataTilePos getLoadTilePos(DataTilePos pos) {
        return new DataTilePos(pos.getTileX() + TILE_OFFSET_X, pos.getTileZ() + TILE_OFFSET_Y);
    }

    @Override
    public CoverRaster getDefaultTile() {
        return DEFAULT_TILE;
    }

    @Nullable
    @Override
    public CoverRaster getForcedTile(DataTilePos pos) {
        DataTilePos loadTilePos = this.getLoadTilePos(pos);
        int x = loadTilePos.getTileX();
        int y = loadTilePos.getTileZ();
        if (x < 0 || y < 0 || x >= TILE_COUNT_X || y >= TILE_COUNT_Y) {
            return DEFAULT_TILE;
        }
        return null;
    }

    @Override
    public SourceResult<CoverRaster> parseStream(DataTilePos pos, InputStream stream) throws IOException {
        try (DataInputStream input = new DataInputStream(stream)) {
            byte[] buffer = new byte[TILE_SIZE * TILE_SIZE];
            input.readFully(buffer);

            CoverType[] cover = new CoverType[buffer.length];
            Arrays.fill(cover, EarthCoverTypes.SNOW);

            for (int y = 0; y < TILE_SIZE; y++) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    byte id = buffer[x + y * TILE_SIZE];
                    cover[x + y * TILE_SIZE] = CoverClassification.get(id).getCoverType();
                }
            }

            return SourceResult.success(new CoverRaster(cover, TILE_SIZE, TILE_SIZE));
        }
    }
}
