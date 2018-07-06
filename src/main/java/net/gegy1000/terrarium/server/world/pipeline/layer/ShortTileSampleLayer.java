package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.util.math.MathHelper;

public class ShortTileSampleLayer extends TiledDataSampleLayer<ShortRasterTile> {
    private final TiledDataSource<? extends ShortRasterTile> source;

    public ShortTileSampleLayer(TiledDataSource<? extends ShortRasterTile> source) {
        super(MathHelper.floor(source.getTileSize().getX()), MathHelper.floor(source.getTileSize().getZ()));
        this.source = source;
    }

    @Override
    public ShortRasterTile apply(DataView view) {
        Handler handler = new Handler(view.getWidth(), view.getHeight());
        this.sampleTiles(handler, view);

        return new ShortRasterTile(handler.data, view.getWidth(), view.getHeight());
    }

    private class Handler implements DataHandler<ShortRasterTile> {
        private final short[] data;
        private final int width;

        private Handler(int width, int height) {
            this.data = new short[width * height];
            this.width = width;
        }

        @Override
        public void put(ShortRasterTile tile, int localX, int localY, int resultX, int resultY) {
            this.data[resultX + resultY * this.width] = tile.getShort(localX, localY);
        }

        @Override
        public ShortRasterTile getTile(DataTilePos pos) {
            return ShortTileSampleLayer.this.source.getTile(pos);
        }
    }
}
