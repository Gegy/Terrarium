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

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class GlobcoverSource extends TiledDataSource<CoverRasterTile> {
    public static final int TILE_SIZE = 2560;
    private static final CoverRasterTile DEFAULT_TILE = new CoverRasterTile(TILE_SIZE, TILE_SIZE);

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

            CoverType[] cover = new CoverType[buffer.length];
            for (int i = 0; i < buffer.length; i++) {
                cover[i] = EarthCoverTypes.Glob.get(buffer[i]).getCoverType();
            }

            return SourceResult.success(new CoverRasterTile(cover, offsetX, offsetZ, width, height));
        }
    }
}
