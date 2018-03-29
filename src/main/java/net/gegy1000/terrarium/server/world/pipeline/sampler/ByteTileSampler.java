package net.gegy1000.terrarium.server.world.pipeline.sampler;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ByteTileSampler extends TiledDataSampler<byte[]> {
    private final TiledDataSource<? extends ByteRasterTileAccess> source;

    public ByteTileSampler(TiledDataSource<? extends ByteRasterTileAccess> source) {
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

    private class Handler implements DataHandler<ByteRasterTileAccess> {
        private final byte[] data;
        private final int width;

        private Handler(int width, int height) {
            this.data = new byte[width * height];
            this.width = width;
        }

        @Override
        public void put(ByteRasterTileAccess tile, int localX, int localZ, int resultX, int resultZ) {
            this.data[resultX + resultZ * this.width] = tile.getByte(localX, localZ);
        }

        @Override
        public ByteRasterTileAccess getTile(DataTilePos pos) {
            return ByteTileSampler.this.source.getTile(pos);
        }
    }

    public static class Parser implements InstanceObjectParser<DataSampler<?>> {
        @Override
        public DataSampler<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            TiledDataSource<ByteRasterTileAccess> source = valueParser.parseTiledSource(objectRoot, "source", ByteRasterTileAccess.class);
            return new ByteTileSampler(source);
        }
    }
}
