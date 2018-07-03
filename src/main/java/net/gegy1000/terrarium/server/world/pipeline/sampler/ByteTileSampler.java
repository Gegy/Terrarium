package net.gegy1000.terrarium.server.world.pipeline.sampler;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;
import net.minecraft.util.math.MathHelper;

public class ByteTileSampler extends TiledDataSampler<byte[]> {
    private final TiledDataSource<? extends ByteRasterTile> source;

    public ByteTileSampler(TiledDataSource<? extends ByteRasterTile> source) {
        super(MathHelper.floor(source.getTileSize().getX()), MathHelper.floor(source.getTileSize().getZ()));
        this.source = source;
    }

    @Override
    public byte[] sample(GenerationSettings settings, int x, int z, int width, int height) {
        Handler handler = new Handler(width, height);
        this.sampleTiles(handler, x, z, width, height);

        return handler.data;
    }

    @Override
    public Class<byte[]> getSamplerType() {
        return byte[].class;
    }

    private class Handler implements DataHandler<ByteRasterTile> {
        private final byte[] data;
        private final int width;

        private Handler(int width, int height) {
            this.data = new byte[width * height];
            this.width = width;
        }

        @Override
        public void put(ByteRasterTile tile, int localX, int localZ, int resultX, int resultZ) {
            this.data[resultX + resultZ * this.width] = tile.getByte(localX, localZ);
        }

        @Override
        public ByteRasterTile getTile(DataTilePos pos) {
            return ByteTileSampler.this.source.getTile(pos);
        }
    }
}
