package net.gegy1000.terrarium.server.world.pipeline.sampler;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.util.math.MathHelper;

public class ShortTileSampler extends TiledDataSampler<short[]> {
    private final TiledDataSource<? extends ShortRasterTile> source;

    public ShortTileSampler(TiledDataSource<? extends ShortRasterTile> source) {
        super(MathHelper.floor(source.getTileSize().getX()), MathHelper.floor(source.getTileSize().getZ()));
        this.source = source;
    }

    @Override
    public short[] sample(GenerationSettings settings, int x, int z, int width, int height) {
        Handler handler = new Handler(width, height);
        this.sampleTiles(handler, x, z, width, height);

        return handler.data;
    }

    @Override
    public Class<short[]> getSamplerType() {
        return short[].class;
    }

    private class Handler implements DataHandler<ShortRasterTile> {
        private final short[] data;
        private final int width;

        private Handler(int width, int height) {
            this.data = new short[width * height];
            this.width = width;
        }

        @Override
        public void put(ShortRasterTile tile, int localX, int localZ, int resultX, int resultZ) {
            this.data[resultX + resultZ * this.width] = tile.getShort(localX, localZ);
        }

        @Override
        public ShortRasterTile getTile(DataTilePos pos) {
            return ShortTileSampler.this.source.getTile(pos);
        }
    }
}
