package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.CoverId;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
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

public class LandCoverSource extends TiledDataSource<EnumRaster<CoverId>> {
    public static final int TILE_SIZE = 1800;
    public static final int GLOBAL_WIDTH = 129600;
    public static final int GLOBAL_HEIGHT = 64800;

    private static final int TILE_COUNT_X = GLOBAL_WIDTH / TILE_SIZE;
    private static final int TILE_COUNT_Y = GLOBAL_HEIGHT / TILE_SIZE;

    private static final int TILE_OFFSET_X = TILE_COUNT_X / 2;
    private static final int TILE_OFFSET_Y = TILE_COUNT_Y / 2;

    private static final EnumRaster<CoverId> DEFAULT_TILE = EnumRaster.createSquare(CoverId.NO_DATA, TILE_SIZE);

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
    public EnumRaster<CoverId> getDefaultTile() {
        return DEFAULT_TILE;
    }

    @Nullable
    @Override
    public EnumRaster<CoverId> getForcedTile(DataTilePos pos) {
        DataTilePos loadTilePos = this.getLoadTilePos(pos);
        int x = loadTilePos.getTileX();
        int y = loadTilePos.getTileZ();
        if (x < 0 || y < 0 || x >= TILE_COUNT_X || y >= TILE_COUNT_Y) {
            return DEFAULT_TILE;
        }
        return null;
    }

    @Override
    public SourceResult<EnumRaster<CoverId>> parseStream(DataTilePos pos, InputStream stream) throws IOException {
        try (DataInputStream input = new DataInputStream(stream)) {
            byte[] buffer = new byte[TILE_SIZE * TILE_SIZE];
            input.readFully(buffer);

            EnumRaster<CoverId> raster = EnumRaster.createSquare(CoverId.PERMANENT_SNOW, TILE_SIZE);
            for (int y = 0; y < TILE_SIZE; y++) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    byte id = buffer[x + y * TILE_SIZE];
                    raster.set(x, y, CoverId.get(id));
                }
            }

            return SourceResult.success(raster);
        }
    }
}
