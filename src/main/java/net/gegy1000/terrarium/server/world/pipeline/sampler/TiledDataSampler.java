package net.gegy1000.terrarium.server.world.pipeline.sampler;

import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;

public abstract class TiledDataSampler<T> implements DataSampler<T> {
    private final int tileWidth;
    private final int tileHeight;

    protected TiledDataSampler(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    protected final <V, H extends DataHandler<V>> void sampleTiles(H dataHandler, int x, int z, int width, int height) {
        int minTileX = Math.floorDiv(x, this.tileWidth);
        int maxTileX = Math.floorDiv((x + width), this.tileWidth);
        int minTileZ = Math.floorDiv(z, this.tileHeight);
        int maxTileZ = Math.floorDiv((z + height), this.tileHeight);
        for (int tileZ = minTileZ; tileZ <= maxTileZ; tileZ++) {
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                DataTilePos pos = new DataTilePos(tileX, tileZ);
                V tile = dataHandler.getTile(pos);
                int minTilePosX = tileX * this.tileWidth;
                int minTilePosZ = tileZ * this.tileHeight;

                int minSampleX = Math.max(0, x - minTilePosX);
                int minSampleZ = Math.max(0, z - minTilePosZ);
                int maxSampleX = Math.min(this.tileWidth, (x + width) - minTilePosX);
                int maxSampleZ = Math.min(this.tileHeight, (z + height) - minTilePosZ);

                for (int localZ = minSampleZ; localZ < maxSampleZ; localZ++) {
                    int resultZ = (localZ + minTilePosZ) - z;
                    for (int localX = minSampleX; localX < maxSampleX; localX++) {
                        int resultX = (localX + minTilePosX) - x;
                        dataHandler.put(tile, localX, localZ, resultX, resultZ);
                    }
                }
            }
        }
    }

    protected interface DataHandler<T> {
        void put(T tile, int localX, int localZ, int resultX, int resultZ);

        T getTile(DataTilePos pos);
    }
}
