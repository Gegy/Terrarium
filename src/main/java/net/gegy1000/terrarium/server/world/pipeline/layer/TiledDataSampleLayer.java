package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataTileKey;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class TiledDataSampleLayer<T extends TiledDataAccess> implements DataLayer<T> {
    private final TiledDataSource<? extends T> source;
    private final int tileWidth;
    private final int tileHeight;

    protected TiledDataSampleLayer(TiledDataSource<? extends T> source, int tileWidth, int tileHeight) {
        this.source = source;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
    }

    protected TiledDataSampleLayer(TiledDataSource<? extends T> source) {
        this.source = source;

        Coordinate tileSize = source.getTileSize();
        this.tileWidth = MathHelper.floor(tileSize.getX());
        this.tileHeight = MathHelper.floor(tileSize.getZ());
    }

    @Override
    public T apply(LayerContext context, DataView view) {
        T result = this.createTile(view);
        this.sampleTiles(context, result, view);
        return result;
    }

    @Override
    public Collection<DataTileKey<?>> getRequiredData(LayerContext context, DataView view) {
        List<DataTileKey<?>> requiredData = new ArrayList<>();

        int minTileX = Math.floorDiv(view.getX(), this.tileWidth);
        int maxTileX = Math.floorDiv((view.getX() + view.getWidth()), this.tileWidth);
        int minTileY = Math.floorDiv(view.getY(), this.tileHeight);
        int maxTileY = Math.floorDiv((view.getY() + view.getHeight()), this.tileHeight);

        for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                requiredData.add(new DataTileKey<>(this.source, tileX, tileY));
            }
        }

        return requiredData;
    }

    protected final void sampleTiles(LayerContext context, T result, DataView view) {
        DataSourceHandler sourceHandler = context.getSourceHandler();

        int minTileX = Math.floorDiv(view.getX(), this.tileWidth);
        int maxTileX = Math.floorDiv((view.getX() + view.getWidth()), this.tileWidth);
        int minTileY = Math.floorDiv(view.getY(), this.tileHeight);
        int maxTileY = Math.floorDiv((view.getY() + view.getHeight()), this.tileHeight);

        for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
            for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
                DataTilePos pos = new DataTilePos(tileX, tileY);
                T tile = sourceHandler.getTile(this.source, pos);

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
                        this.copy(tile, result, localX, localY, resultX, resultY);
                    }
                }
            }
        }
    }

    protected abstract T createTile(DataView view);

    protected abstract void copy(T origin, T target, int originX, int originY, int targetX, int targetY);
}
