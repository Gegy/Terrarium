package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;

public class CoverTileSampleLayer extends TiledDataSampleLayer<BiomeRasterTile> {
    public CoverTileSampleLayer(TiledDataSource<? extends BiomeRasterTile> source) {
        super(source);
    }

    @Override
    protected BiomeRasterTile createTile(DataView view) {
        return new BiomeRasterTile(view);
    }

    @Override
    protected void copy(BiomeRasterTile origin, BiomeRasterTile target, int originX, int originY, int targetX, int targetY) {
        target.set(targetX, targetY, origin.get(originX, originY));
    }
}
