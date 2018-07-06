package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;
import net.minecraft.util.math.MathHelper;

public class ByteTileSampleLayer extends TiledDataSampleLayer<ByteRasterTile> {
    private final TiledDataSource<? extends ByteRasterTile> source;

    public ByteTileSampleLayer(TiledDataSource<? extends ByteRasterTile> source) {
        super(MathHelper.floor(source.getTileSize().getX()), MathHelper.floor(source.getTileSize().getZ()));
        this.source = source;
    }

    @Override
    public ByteRasterTile apply(DataView view) {
        Handler handler = new Handler(view.getWidth(), view.getHeight());
        this.sampleTiles(handler, view);

        return new ByteRasterTile(handler.data, view.getWidth(), view.getHeight());
    }

    private class Handler implements DataHandler<ByteRasterTile> {
        private final byte[] data;
        private final int width;

        private Handler(int width, int height) {
            this.data = new byte[width * height];
            this.width = width;
        }

        @Override
        public void put(ByteRasterTile tile, int localX, int localY, int resultX, int resultY) {
            this.data[resultX + resultY * this.width] = tile.getByte(localX, localY);
        }

        @Override
        public ByteRasterTile getTile(DataTilePos pos) {
            return ByteTileSampleLayer.this.source.getTile(pos);
        }
    }
}
