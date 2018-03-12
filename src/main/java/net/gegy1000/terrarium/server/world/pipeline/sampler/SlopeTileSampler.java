package net.gegy1000.terrarium.server.world.pipeline.sampler;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTileAccess;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SlopeTileSampler implements DataSampler<byte[]> {
    private final ShortTileSampler heightSampler;

    public SlopeTileSampler(TiledDataSource<ShortRasterTileAccess> heightSource) {
        this.heightSampler = new ShortTileSampler(heightSource);
    }

    @Override
    public byte[] sample(GenerationSettings settings, int x, int z, int width, int height) {
        byte[] output = new byte[width * height];

        int sampleWidth = width + 2;
        int sampleHeight = height + 2;
        short[] sampled = this.heightSampler.sample(settings, x - 1, z - 1, sampleWidth, sampleHeight);

        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int sampleIndex = localX + 1 + (localZ + 1) * sampleWidth;

                short current = sampled[sampleIndex];

                int left = Math.abs(current - sampled[sampleIndex - 1]);
                int right = Math.abs(current - sampled[sampleIndex + 1]);
                int top = Math.abs(current - sampled[sampleIndex - sampleWidth]);
                int bottom = Math.abs(current - sampled[sampleIndex + sampleWidth]);

                int maxSlope = Math.max(left, Math.max(right, Math.max(top, bottom)));
                output[localX + localZ * width] = (byte) MathHelper.clamp(maxSlope, 0, 255);
            }
        }

        return output;
    }

    @Override
    public Class<byte[]> getSamplerType() {
        return byte[].class;
    }

    public static class Parser implements InstanceObjectParser<DataSampler<?>> {
        @Override
        public DataSampler<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            TiledDataSource<ShortRasterTileAccess> source = valueParser.parseTiledSource(objectRoot, "height_source", ShortRasterTileAccess.class);
            return new SlopeTileSampler(source);
        }
    }
}
