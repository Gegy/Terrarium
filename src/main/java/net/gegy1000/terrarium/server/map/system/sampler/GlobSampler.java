package net.gegy1000.terrarium.server.map.system.sampler;

import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.source.glob.GlobSource;
import net.gegy1000.terrarium.server.map.source.raster.RasterDataAccess;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class GlobSampler implements DataSampler<CoverType[]> {
    private final TiledSource<? extends RasterDataAccess<CoverType>> globSource;

    public GlobSampler(TiledSource<? extends RasterDataAccess<CoverType>> globSource) {
        this.globSource = globSource;
    }

    @Override
    public CoverType[] sample(EarthGenerationSettings settings, int x, int z, int width, int height) {
        // TODO: Come back to more efficient, but broken algorithm
        CoverType[] output = ArrayUtils.defaulted(new CoverType[width * height], CoverType.NO_DATA);
        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                output[localX + localZ * width] = this.getCover(x + localX, z + localZ);
            }
        }
        return output;
    }

    private CoverType getCover(double globX, double globZ) {
        int roundedGlobX = MathHelper.floor(globX);
        int roundedGlobZ = MathHelper.floor(globZ);

        DataTilePos pos = new DataTilePos(Math.floorDiv(roundedGlobX, GlobSource.TILE_SIZE), Math.floorDiv(roundedGlobZ, GlobSource.TILE_SIZE));
        RasterDataAccess<CoverType> tile = this.globSource.getTile(pos);

        int minTileX = pos.getTileX() * GlobSource.TILE_SIZE;
        int minTileZ = pos.getTileY() * GlobSource.TILE_SIZE;
        return tile.get(roundedGlobX - minTileX, roundedGlobZ - minTileZ);
    }
}
