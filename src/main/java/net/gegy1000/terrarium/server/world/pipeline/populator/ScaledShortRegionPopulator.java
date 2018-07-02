package net.gegy1000.terrarium.server.world.pipeline.populator;

import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.sampler.DataSampler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;

public class ScaledShortRegionPopulator extends InterpolatingRegionPopulator<ShortRasterTile> {
    private final DataSampler<short[]> sampler;

    public ScaledShortRegionPopulator(DataSampler<short[]> sampler, CoordinateState coordinateState, Interpolation.Method interpolationMethod) {
        super(interpolationMethod, coordinateState);
        this.sampler = sampler;
    }

    @Override
    protected ShortRasterTile populate(GenerationSettings settings, int minSampleX, int minSampleZ,
                                       int sampleWidth, int sampleHeight, int width, int height,
                                       double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ
    ) {
        ShortRaster sampledHeights = new ShortRaster(this.sampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight), sampleWidth, sampleHeight);
        ShortRaster resultHeights = new ShortRaster(new short[width * height], width, height);

        this.scaleRegion(sampledHeights, resultHeights, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);

        return new ShortRasterTile(resultHeights.data, width, height);
    }

    @Override
    public Class<ShortRasterTile> getType() {
        return ShortRasterTile.class;
    }

    private class ShortRaster implements DataHandler {
        private final short[] data;
        private final int width;
        private final int height;

        private ShortRaster(short[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }

        @Override
        public void set(int x, int z, double value) {
            this.data[x + z * this.width] = (short) value;
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
