package net.gegy1000.earth.server.world.data.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.CoverId;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.EnumRaster;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import org.apache.commons.io.IOUtils;
import org.tukaani.xz.SingleXZInputStream;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

public class LandCoverSource extends TiledDataSource<EnumRaster<CoverId>> {
    public static final int TILE_SIZE = 1800;
    public static final int GLOBAL_WIDTH = 129600;
    public static final int GLOBAL_HEIGHT = 64800;

    private static final int TILE_COUNT_X = GLOBAL_WIDTH / TILE_SIZE;
    private static final int TILE_COUNT_Y = GLOBAL_HEIGHT / TILE_SIZE;

    private static final int TILE_OFFSET_X = TILE_COUNT_X / 2;
    private static final int TILE_OFFSET_Y = TILE_COUNT_Y / 2;

    private static final EnumRaster<CoverId> DEFAULT_RESULT = EnumRaster.createSquare(CoverId.NO_DATA, TILE_SIZE);

    private static final Path CACHE_ROOT = GLOBAL_CACHE_ROOT.resolve("landcover");

    private static final CachingInput<DataTilePos> CACHING_INPUT = CachingInput.<DataTilePos>create()
            .cachesTo(LandCoverSource::resolveCachePath);

    public LandCoverSource(CoordinateState coordinateState) {
        super(new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE));
    }

    private static Path resolveCachePath(DataTilePos pos) {
        return CACHE_ROOT.resolve(pos.getTileX() + "_" + pos.getTileZ() + ".lc");
    }

    @Override
    public Optional<EnumRaster<CoverId>> load(DataTilePos pos) throws IOException {
        String url = EarthRemoteIndex.get().landcover.getUrlFor(pos);
        if (url == null) {
            return Optional.empty();
        }

        InputStream sourceInput = CACHING_INPUT.getInputStream(pos, p -> {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", TerrariumEarth.USER_AGENT);
            return connection.getInputStream();
        });

        try (InputStream input = new SingleXZInputStream(new BufferedInputStream(sourceInput))) {
            return Optional.of(this.parseStream(input));
        }
    }

    @Override
    public EnumRaster<CoverId> getDefaultResult() {
        return DEFAULT_RESULT;
    }

    private EnumRaster<CoverId> parseStream(InputStream input) throws IOException {
        byte[] bytes = IOUtils.readFully(input, TILE_SIZE * TILE_SIZE);

        EnumRaster<CoverId> raster = EnumRaster.createSquare(CoverId.PERMANENT_SNOW, TILE_SIZE);
        for (int y = 0; y < TILE_SIZE; y++) {
            for (int x = 0; x < TILE_SIZE; x++) {
                byte id = bytes[x + y * TILE_SIZE];
                raster.set(x, y, CoverId.get(id));
            }
        }

        return raster;
    }
}
