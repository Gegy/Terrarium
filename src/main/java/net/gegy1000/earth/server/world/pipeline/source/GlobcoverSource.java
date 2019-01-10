package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceResult;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.minecraft.util.ResourceLocation;
import org.tukaani.xz.SingleXZInputStream;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class GlobcoverSource extends TiledDataSource<CoverRasterTile> {
    public static final int TILE_SIZE = 2560;
    private static final CoverRasterTile DEFAULT_TILE = new CoverRasterTile(TILE_SIZE, TILE_SIZE);

    private static final int MIN_X = -26;
    private static final int MIN_Y = -13;

    private static final int MAX_X = 25;
    private static final int MAX_Y = 9;

    static {
        CoverType[] data = DEFAULT_TILE.getData();
        Arrays.fill(data, EarthCoverTypes.SNOW);
    }

    public GlobcoverSource(CoordinateState coordinateState, String cacheRoot) {
        super(new ResourceLocation(TerrariumEarth.MODID, "globcover"), new File(GLOBAL_CACHE_ROOT, cacheRoot), new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE));
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        URL url = new URL(String.format("%s/%s/%s", EarthRemoteData.info.getBaseURL(), EarthRemoteData.info.getGlobEndpoint(), this.getCachedName(key)));
        return url.openStream();
    }

    @Override
    public InputStream getWrappedStream(InputStream stream) throws IOException {
        return new SingleXZInputStream(stream);
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format(EarthRemoteData.info.getGlobQuery(), key.getTileX(), key.getTileZ());
    }

    @Override
    public CoverRasterTile getDefaultTile() {
        return DEFAULT_TILE;
    }

    @Nullable
    @Override
    public CoverRasterTile getLocalTile(DataTilePos pos) {
        int x = pos.getTileX();
        int y = pos.getTileZ();
        if (x < MIN_X || y < MIN_Y || x > MAX_X || y > MAX_Y) {
            return DEFAULT_TILE;
        }
        return null;
    }

    @Override
    public SourceResult<CoverRasterTile> parseStream(DataTilePos pos, InputStream stream) throws IOException {
        try (DataInputStream input = new DataInputStream(stream)) {
            int width = input.readUnsignedShort();
            int height = input.readUnsignedShort();

            if (width > TILE_SIZE || height > TILE_SIZE) {
                return SourceResult.malformed("Globcover tile was of unexpected size " + width + "x" + height);
            }

            int offsetX = pos.getTileX() < 0 ? TILE_SIZE - width : 0;
            int offsetZ = pos.getTileZ() < 0 ? TILE_SIZE - height : 0;

            byte[] buffer = new byte[width * height];
            input.readFully(buffer);

            CoverType[] cover = new CoverType[TILE_SIZE * TILE_SIZE];
            Arrays.fill(cover, EarthCoverTypes.SNOW);

            for (int bufferZ = 0; bufferZ < height; bufferZ++) {
                for (int bufferX = 0; bufferX < width; bufferX++) {
                    int tileX = bufferX + offsetX;
                    int tileZ = bufferZ + offsetZ;
                    byte globId = buffer[bufferX + bufferZ * width];
                    cover[tileX + tileZ * TILE_SIZE] = EarthCoverTypes.Glob.get(globId).getCoverType();
                }
            }

            return SourceResult.success(new CoverRasterTile(cover, TILE_SIZE, TILE_SIZE));
        }
    }
}
