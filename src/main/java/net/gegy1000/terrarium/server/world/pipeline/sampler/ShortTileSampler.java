package net.gegy1000.terrarium.server.world.pipeline.sampler;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ShortTileSampler extends TiledDataSampler<short[]> {
    private final TiledDataSource<? extends ShortRasterTileAccess> source;

    public ShortTileSampler(TiledDataSource<? extends ShortRasterTileAccess> source) {
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

    private class Handler implements DataHandler<ShortRasterTileAccess> {
        private final short[] data;
        private final int width;

        private Handler(int width, int height) {
            this.data = new short[width * height];
            this.width = width;
        }

        @Override
        public void put(ShortRasterTileAccess tile, int localX, int localZ, int resultX, int resultZ) {
            this.data[resultX + resultZ * this.width] = tile.getShort(localX, localZ);
        }

        @Override
        public ShortRasterTileAccess getTile(DataTilePos pos) {
            return ShortTileSampler.this.source.getTile(pos);
        }
    }

    public static class Parser implements InstanceObjectParser<DataSampler<?>> {
        @Override
        public DataSampler<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            TiledDataSource<ShortRasterTileAccess> source = valueParser.parseTiledSource(objectRoot, "source", ShortRasterTileAccess.class);
            return new ShortTileSampler(source);
        }
    }
}
