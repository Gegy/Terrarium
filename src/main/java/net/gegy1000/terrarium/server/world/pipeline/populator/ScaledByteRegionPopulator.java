package net.gegy1000.terrarium.server.world.pipeline.populator;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTileAccess;
import net.minecraft.world.World;

public class ScaledByteRegionPopulator extends InterpolatingRegionPopulator<ByteRasterTileAccess> {
    private final DataSampler<byte[]> sampler;

    public ScaledByteRegionPopulator(DataSampler<byte[]> sampler, CoordinateState coordinateState, Interpolation.Method interpolationMethod) {
        super(interpolationMethod, coordinateState);
        this.sampler = sampler;
    }

    @Override
    protected ByteRasterTileAccess populate(GenerationSettings settings, int minSampleX, int minSampleZ,
                                            int sampleWidth, int sampleHeight, int width, int height,
                                            double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ
    ) {
        ByteRaster sampledHeights = new ByteRaster(this.sampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight), sampleWidth, sampleHeight);
        ByteRaster resultHeights = new ByteRaster(new byte[width * height], width, height);

        this.scaleRegion(sampledHeights, resultHeights, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);

        return new ByteRasterTileAccess(resultHeights.data, width, height);
    }

    @Override
    public Class<ByteRasterTileAccess> getType() {
        return ByteRasterTileAccess.class;
    }

    public static class Parser implements InstanceObjectParser<RegionPopulator<?>> {
        @Override
        public RegionPopulator<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            DataSampler<byte[]> sampler = valueParser.parseSampler(objectRoot, "sampler", byte[].class);
            CoordinateState coordinateState = valueParser.parseCoordinateState(objectRoot, "coordinate");
            Interpolation.Method interpolationMethod = Interpolation.Method.parse(valueParser.parseString(objectRoot, "interpolation_method"));

            return new ScaledByteRegionPopulator(sampler, coordinateState, interpolationMethod);
        }
    }

    private class ByteRaster implements DataHandler {
        private final byte[] data;
        private final int width;
        private final int height;

        private ByteRaster(byte[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        @Override
        public void set(int x, int z, double value) {
            this.data[x + z * this.width] = (byte) value;
        }

        @Override
        public double get(int x, int z) {
            return this.data[x + z * this.width];
        }

        @Override
        public int getWidth() {
            return this.width;
        }

        @Override
        public int getHeight() {
            return this.height;
        }
    }
}
