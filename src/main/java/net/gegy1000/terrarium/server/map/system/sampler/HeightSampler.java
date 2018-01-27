package net.gegy1000.terrarium.server.map.system.sampler;

import net.gegy1000.terrarium.server.map.source.raster.ShortRasterDataAccess;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class HeightSampler implements DataSampler<short[]> {
    private final TiledSource<? extends ShortRasterDataAccess> heightSource;

    public HeightSampler(TiledSource<? extends ShortRasterDataAccess> heightSource) {
        this.heightSource = heightSource;
    }

    @Override
    public short[] sample(EarthGenerationSettings settings, int x, int z, int width, int height) {
        short[] output = new short[width * height];

        Coordinate minimumCoordinate = new Coordinate(settings, x, z);
        Coordinate maximumCoordinate = new Coordinate(settings, x + width, z + height);

        for (int tileLatitude = MathHelper.floor(maximumCoordinate.getLatitude()); tileLatitude <= MathHelper.floor(minimumCoordinate.getLatitude()); tileLatitude++) {
            for (int tileLongitude = MathHelper.floor(minimumCoordinate.getLongitude()); tileLongitude <= MathHelper.floor(maximumCoordinate.getLongitude()); tileLongitude++) {
                DataTilePos pos = new DataTilePos(tileLongitude, tileLatitude);
                ShortRasterDataAccess tile = this.heightSource.getTile(pos);
                int minTileX = pos.getTileX() * 1200;
                int minTileZ = -(pos.getTileY() + 1) * 1200;

                int minSampleX = Math.max(0, x - minTileX);
                int minSampleZ = Math.max(0, z - minTileZ);
                int maxSampleX = Math.min(1200, (x + width) - minTileX);
                int maxSampleZ = Math.min(1200, (z + height) - minTileZ);

                for (int localZ = minSampleZ; localZ < maxSampleZ; localZ++) {
                    int resultZ = (localZ + minTileZ) - z;
                    for (int localX = minSampleX; localX < maxSampleX; localX++) {
                        int resultX = (localX + minTileX) - x;

                        short heightValue = tile.getShort(localX, localZ);
                        if (heightValue >= 0) {
                            output[resultX + resultZ * width] = heightValue;
                        }
                    }
                }
            }
        }

        return output;
    }
}
