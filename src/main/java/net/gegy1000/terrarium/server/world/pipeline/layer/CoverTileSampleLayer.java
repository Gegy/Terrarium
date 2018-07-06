package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;
import net.minecraft.util.math.MathHelper;

public class CoverTileSampleLayer extends TiledDataSampleLayer<CoverRasterTile> {
    private final TiledDataSource<? extends RasterDataAccess<CoverType>> source;

    public CoverTileSampleLayer(TiledDataSource<? extends RasterDataAccess<CoverType>> source) {
        super(MathHelper.floor(source.getTileSize().getX()), MathHelper.floor(source.getTileSize().getZ()));
        this.source = source;
    }

    @Override
    public CoverRasterTile apply(DataView view) {
        Handler handler = new Handler(view.getWidth(), view.getHeight());
        this.sampleTiles(handler, view);

        return new CoverRasterTile(handler.data, view.getWidth(), view.getHeight());
    }

    private class Handler implements DataHandler<RasterDataAccess<CoverType>> {
        private final CoverType[] data;
        private final int width;

        private Handler(int width, int height) {
            this.data = ArrayUtils.defaulted(new CoverType[width * height], TerrariumCoverTypes.PLACEHOLDER);
            this.width = width;
        }

        @Override
        public void put(RasterDataAccess<CoverType> tile, int localX, int localY, int resultX, int resultY) {
            this.data[resultX + resultY * this.width] = tile.get(localX, localY);
        }

        @Override
        public RasterDataAccess<CoverType> getTile(DataTilePos pos) {
            return CoverTileSampleLayer.this.source.getTile(pos);
        }
    }
}
