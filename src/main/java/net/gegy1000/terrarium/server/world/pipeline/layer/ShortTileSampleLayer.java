package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

public class ShortTileSampleLayer extends TiledDataSampleLayer<ShortRasterTile> {
    public ShortTileSampleLayer(TiledDataSource<? extends ShortRasterTile> source) {
        super(source);
    }

    @Override
    protected ShortRasterTile createTile(DataView view) {
        return new ShortRasterTile(view);
    }

    @Override
    protected void copy(ShortRasterTile origin, ShortRasterTile target, int originX, int originY, int targetX, int targetY) {
        target.setShort(targetX, targetY, origin.getShort(originX, originY));
    }
}
