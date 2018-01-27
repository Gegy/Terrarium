package net.gegy1000.terrarium.server.map.system.sampler;

import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.glob.GlobSource;
import net.gegy1000.terrarium.server.map.source.raster.RasterDataAccess;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class GlobSampler implements DataSampler<CoverType[]> {
    private final TiledSource<? extends RasterDataAccess<CoverType>> globSource;

    public GlobSampler(TiledSource<? extends RasterDataAccess<CoverType>> globSource) {
        this.globSource = globSource;
    }

    @Override
    public CoverType[] sample(EarthGenerationSettings settings, int x, int z, int width, int height) {
        CoverType[] output = ArrayUtils.defaulted(new CoverType[width * height], CoverType.NO_DATA);

        for (int tileZ = Math.floorDiv(z, GlobSource.TILE_SIZE); tileZ <= Math.floorDiv(z + width, GlobSource.TILE_SIZE); tileZ++) {
            for (int tileX = Math.floorDiv(x, GlobSource.TILE_SIZE); tileX <= Math.floorDiv(x + width, GlobSource.TILE_SIZE); tileX++) {
                DataTilePos pos = new DataTilePos(tileX, tileZ);
                RasterDataAccess<CoverType> tile = this.globSource.getTile(pos);
                int minTileX = pos.getTileX() * GlobSource.TILE_SIZE;
                int minTileZ = pos.getTileY() * GlobSource.TILE_SIZE;

                int minSampleX = Math.max(0, x - minTileX);
                int minSampleZ = Math.max(0, z - minTileZ);
                int maxSampleX = Math.min(GlobSource.TILE_SIZE, (x + width) - minTileX);
                int maxSampleZ = Math.min(GlobSource.TILE_SIZE, (z + height) - minTileZ);

                for (int localZ = minSampleZ; localZ < maxSampleZ; localZ++) {
                    int resultZ = (localZ + minTileZ) - z;
                    for (int localX = minSampleX; localX < maxSampleX; localX++) {
                        int resultX = (localX + minTileX) - x;
                        output[resultX + resultZ * width] = tile.get(localX, localZ);
                    }
                }
            }
        }

        return output;
    }
}
