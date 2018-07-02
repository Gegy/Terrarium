package net.gegy1000.terrarium.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ByteRasterTile;

public class ScaledByteRegionPopulator extends InterpolatingRegionPopulator<ByteRasterTile> {
    private final DataSampler<byte[]> sampler;

    public ScaledByteRegionPopulator(DataSampler<byte[]> sampler, CoordinateState coordinateState, Interpolation.Method interpolationMethod) {
        super(interpolationMethod, coordinateState);
        this.sampler = sampler;
    }

    @Override
    protected ByteRasterTile populate(GenerationSettings settings, int minSampleX, int minSampleZ,
                                      int sampleWidth, int sampleHeight, int width, int height,
                                      double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ
    ) {
        ByteRaster sampledHeights = new ByteRaster(this.sampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight), sampleWidth, sampleHeight);
        ByteRaster resultHeights = new ByteRaster(new byte[width * height], width, height);

        this.scaleRegion(sampledHeights, resultHeights, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);

        return new ByteRasterTile(resultHeights.data, width, height);
    }

    @Override
    public Class<ByteRasterTile> getType() {
        return ByteRasterTile.class;
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
