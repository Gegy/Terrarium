package net.gegy1000.earth.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.cover.EarthCoverBiomes;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceResult;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import org.tukaani.xz.SingleXZInputStream;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class GlobcoverSource extends TiledDataSource<BiomeRasterTile> {
    public static final int TILE_SIZE = 2560;
    private static final BiomeRasterTile DEFAULT_TILE = new BiomeRasterTile(TILE_SIZE, TILE_SIZE);

    public GlobcoverSource(CoordinateState coordinateState, String cacheRoot) {
        super(new Identifier(TerrariumEarth.MODID, "globcover"), new File(GLOBAL_CACHE_ROOT, cacheRoot), new Coordinate(coordinateState, TILE_SIZE, TILE_SIZE));
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
    public BiomeRasterTile getDefaultTile() {
        return DEFAULT_TILE;
    }

    @Override
    public SourceResult<BiomeRasterTile> parseStream(DataTilePos pos, InputStream stream) throws IOException {
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

            Biome[] biomes = new Biome[buffer.length];
            for (int i = 0; i < buffer.length; i++) {
                biomes[i] = EarthCoverBiomes.Glob.get(buffer[i]).getBiome();
            }

            return SourceResult.success(new BiomeRasterTile(biomes, offsetX, offsetZ, width, height));
        }
    }
}
