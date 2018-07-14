package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;

public class CoverTileSampleLayer extends TiledDataSampleLayer<CoverRasterTile> {
    public CoverTileSampleLayer(TiledDataSource<? extends CoverRasterTile> source) {
        super(source);
    }

    @Override
    protected CoverRasterTile createTile(DataView view) {
        return new CoverRasterTile(view);
    }

    @Override
    protected void copy(CoverRasterTile origin, CoverRasterTile target, int originX, int originY, int targetX, int targetY) {
        target.set(targetX, targetY, origin.get(originX, originY));
    }
}
