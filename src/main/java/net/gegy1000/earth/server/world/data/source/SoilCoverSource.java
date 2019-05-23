package net.gegy1000.earth.server.world.data.source;

// TODO
public class SoilCoverSource {
    public static final int GLOBAL_WIDTH = 172800;
    public static final int GLOBAL_HEIGHT = 86400;
}

/*
package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ObjRaster;
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

public class SoilCoverSource extends TiledDataSource<ObjRaster<SoilClassification>> {
    public static final int TILE_SIZE = 1800;
    public static final int GLOBAL_WIDTH = 172800;
    public static final int GLOBAL_HEIGHT = 86400;

    private static final int TILE_COUNT_X = GLOBAL_WIDTH / TILE_SIZE;
    private static final int TILE_COUNT_Y = GLOBAL_HEIGHT / TILE_SIZE;

    private static final int TILE_OFFSET_X = TILE_COUNT_X / 2;
    private static final int TILE_OFFSET_Y = TILE_COUNT_Y / 2;

    private static final ObjRaster<SoilClassification> DEFAULT_TILE = ObjRaster.create(SoilClassifcation, TILE_SIZE, TILE_SIZE);

    public SoilCoverSource(CoordinateState coordinateState, String cacheRoot) {
        super(new ResourceLocation(TerrariumEarth.MODID, "soil"), new File(GLOBAL_CACHE_ROOT, cacheRoot), new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE));
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        URL url = new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getSoilEndpoint(), this.getCachePath(key)));
        return url.openStream();
    }

    @Override
    public InputStream getWrappedStream(InputStream stream) throws IOException {
        return new SingleXZInputStream(stream);
    }

    @Override
    public String getCachePath(DataTilePos key) {
        return String.format(EarthRemoteData.info.getSoilQuery(), key.getTileX(), key.getTileZ());
    }

    @Override
    public DataTilePos getLoadTilePos(DataTilePos pos) {
        return new DataTilePos(pos.getTileX() + TILE_OFFSET_X, pos.getTileZ() + TILE_OFFSET_Y);
    }

    @Override
    public SoilClassificationRaster getDefaultTile() {
        return DEFAULT_TILE;
    }

    @Nullable
    @Override
    public SoilClassificationRaster getForcedTile(DataTilePos pos) {
        DataTilePos loadTilePos = this.getLoadTilePos(pos);
        int x = loadTilePos.getTileX();
        int y = loadTilePos.getTileZ();
        if (x < 0 || y < 0 || x >= TILE_COUNT_X || y >= TILE_COUNT_Y) {
            return DEFAULT_TILE;
        }
        return null;
    }

    @Override
    public SourceResult<SoilClassificationRaster> parseStream(DataTilePos pos, InputStream stream) throws IOException {
        try (DataInputStream input = new DataInputStream(stream)) {
            byte[] buffer = new byte[TILE_SIZE * TILE_SIZE];
            input.readFully(buffer);

            SoilClassification[] soil = new SoilClassification[buffer.length];
            Arrays.fill(soil, SoilClassification.NOT_SOIL);

            for (int y = 0; y < TILE_SIZE; y++) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    byte id = buffer[x + y * TILE_SIZE];
                    soil[x + y * TILE_SIZE] = SoilClassification.get(id);
                }
            }

            return SourceResult.ok(new SoilClassificationRaster(soil, TILE_SIZE, TILE_SIZE));
        }
    }
}
*/
