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

    /*val minimumX = MathHelper.floor(minimumCoordinate.globalX)
    val minimumZ = MathHelper.floor(minimumCoordinate.globalZ)
    val maximumX = MathHelper.floor(maximumCoordinate.globalX)
    val maximumZ = MathHelper.floor(maximumCoordinate.globalZ)
    for (tileLatitude in MathHelper.floor(minimumCoordinate.latitude)..MathHelper.floor(maximumCoordinate.latitude)) {
        for (tileLongitude in MathHelper.floor(minimumCoordinate.longitude)..MathHelper.floor(maximumCoordinate.longitude)) {
            val pos = DataTilePos(tileLongitude, tileLatitude)
            val minX = pos.tileX * 1200
            val minZ = (-pos.tileY - 1) * 1200
            val tile = this.getTile(pos)
            val minSampleZ = Math.max(0, minimumZ - minZ)
            val maxSampleZ = Math.min(1200, maximumZ - minZ)
            val minSampleX = Math.max(0, minimumX - minX)
            val maxSampleX = Math.min(1200, maximumX - minX)
            for (z in minSampleZ..maxSampleZ - 1) {
                val globalZ = z + minZ
                val resultZ = globalZ - minimumZ
                val resultIndexZ = resultZ * width
                for (x in minSampleX..maxSampleX - 1) {
                    val globalX = x + minX
                    val resultX = globalX - minimumX
                    result[resultX + resultIndexZ] = tile.get(x, z)
                }
            }
        }
    }*/

    @Override
    public short[] sample(EarthGenerationSettings settings, int x, int z, int width, int height) {
        // TODO: Come back to more efficient, but broken algorithm
        short[] output = new short[width * height];
        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                short heightValue = this.getHeight(settings, x + localX, z + localZ);
                if (heightValue >= 0) {
                    output[localX + localZ * width] = heightValue;
                }
            }
        }
        return output;
    }

    private short getHeight(EarthGenerationSettings settings, int x, int z) {
        DataTilePos tilePos = this.getTilePos(settings, x, z);
        ShortRasterDataAccess tile = this.heightSource.getTile(tilePos);

        int minTileX = tilePos.getTileX() * 1200;
        int minTileZ = -(tilePos.getTileY() + 1) * 1200;

        return tile.get(x - minTileX, z - minTileZ);
    }

    private DataTilePos getTilePos(EarthGenerationSettings settings, int x, int z) {
        Coordinate coordinate = new Coordinate(settings, x, z);
        return new DataTilePos(MathHelper.floor(coordinate.getLongitude()), MathHelper.floor(coordinate.getLatitude()));
    }
}
