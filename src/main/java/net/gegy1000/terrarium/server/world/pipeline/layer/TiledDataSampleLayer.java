package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

public abstract class TiledDataSampleLayer<T extends TiledDataAccess> implements DataLayerProducer<T> {
    private final int tileWidth;
    private final int tileHeight;

    protected TiledDataSampleLayer(int tileWidth, int tileHeight) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    protected final <V, H extends DataHandler<V>> void sampleTiles(H dataHandler, DataView view) {
        int minTileX = Math.floorDiv(view.getX(), this.tileWidth);
        int maxTileX = Math.floorDiv((view.getX() + view.getWidth()), this.tileWidth);
        int minTileY = Math.floorDiv(view.getY(), this.tileHeight);
        int maxTileY = Math.floorDiv((view.getY() + view.getHeight()), this.tileHeight);
        for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                DataTilePos pos = new DataTilePos(tileX, tileY);
                V tile = dataHandler.getTile(pos);
                int minTilePosX = tileX * this.tileWidth;
                int minTilePosY = tileY * this.tileHeight;

                int minSampleX = Math.max(0, view.getX() - minTilePosX);
                int minSampleY = Math.max(0, view.getY() - minTilePosY);
                int maxSampleX = Math.min(this.tileWidth, (view.getX() + view.getWidth()) - minTilePosX);
                int maxSampleY = Math.min(this.tileHeight, (view.getY() + view.getHeight()) - minTilePosY);

                for (int localY = minSampleY; localY < maxSampleY; localY++) {
                    int resultY = (localY + minTilePosY) - view.getY();
                    for (int localX = minSampleX; localX < maxSampleX; localX++) {
                        int resultX = (localX + minTilePosX) - view.getX();
                        dataHandler.put(tile, localX, localY, resultX, resultY);
                    }
                }
            }
        }
    }

    protected interface DataHandler<T> {
        void put(T tile, int localX, int localY, int resultX, int resultY);

        T getTile(DataTilePos pos);
    }
}
