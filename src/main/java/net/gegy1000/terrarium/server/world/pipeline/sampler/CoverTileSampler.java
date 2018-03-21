package net.gegy1000.terrarium.server.world.pipeline.sampler;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.CoverTypeRegistry;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.RasterDataAccess;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class CoverTileSampler extends TiledDataSampler<CoverType[]> {
    private final TiledDataSource<? extends RasterDataAccess<CoverType>> source;

    public CoverTileSampler(TiledDataSource<? extends RasterDataAccess<CoverType>> source) {
        super(MathHelper.floor(source.getTileSize().getX()), MathHelper.floor(source.getTileSize().getZ()));
        this.source = source;
    }

    @Override
    public CoverType[] sample(GenerationSettings settings, int x, int z, int width, int height) {
        Handler handler = new Handler(width, height);
        this.sampleTiles(handler, x, z, width, height);

        return handler.data;
    }

    @Override
    public Class<CoverType[]> getSamplerType() {
        return CoverType[].class;
    }

    private class Handler implements DataHandler<RasterDataAccess<CoverType>> {
        private final CoverType[] data;
        private final int width;

        private Handler(int width, int height) {
            this.data = ArrayUtils.defaulted(new CoverType[width * height], CoverTypeRegistry.PLACEHOLDER);
            this.width = width;
        }

        @Override
        public void put(RasterDataAccess<CoverType> tile, int localX, int localZ, int resultX, int resultZ) {
            this.data[resultX + resultZ * this.width] = tile.get(localX, localZ);
        }

        @Override
        public RasterDataAccess<CoverType> getTile(DataTilePos pos) {
            return CoverTileSampler.this.source.getTile(pos);
        }
    }

    public static class Parser implements InstanceObjectParser<DataSampler<?>> {
        @Override
        public DataSampler<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            TiledDataSource<CoverRasterTileAccess> source = valueParser.parseTiledSource(objectRoot, "source", CoverRasterTileAccess.class);
            return new CoverTileSampler(source);
        }
    }
}
