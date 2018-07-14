package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;

public class ByteTileSampleLayer extends TiledDataSampleLayer<ByteRasterTile> {
    public ByteTileSampleLayer(TiledDataSource<? extends ByteRasterTile> source) {
        super(source);
    }

    @Override
    protected ByteRasterTile createTile(DataView view) {
        return new ByteRasterTile(view);
    }

    @Override
    protected void copy(ByteRasterTile origin, ByteRasterTile target, int originX, int originY, int targetX, int targetY) {
        target.setByte(targetX, targetY, origin.getByte(originX, originY));
    }
}
