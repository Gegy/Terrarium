package net.gegy1000.terrarium.server.map.system.populator;

import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.map.source.height.SlopeTileAccess;
import net.gegy1000.terrarium.server.map.system.sampler.DataSampler;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class SlopeRegionPopulator extends BufferedScalingPopulator<SlopeTileAccess> {
    private static final int LOWER_SAMPLE_BUFFER = 0;
    private static final int UPPER_SAMPLE_BUFFER = 1;

    private final DataSampler<byte[]> slopeSampler;

    public SlopeRegionPopulator(DataSampler<byte[]> slopeSampler) {
        super(LOWER_SAMPLE_BUFFER, UPPER_SAMPLE_BUFFER, Coordinate::getGlobalX, Coordinate::getGlobalZ);
        this.slopeSampler = slopeSampler;
    }

    @Override
    protected SlopeTileAccess populate(EarthGenerationSettings settings, RegionTilePos pos, int minSampleX, int minSampleZ,
                                        int sampleWidth, int sampleHeight, int width, int height,
                                        double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ
    ) {
        byte[] sampledHeights = this.slopeSampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight);
        byte[] resultHeights = this.scaleHeightRegion(sampledHeights, sampleWidth, width, height, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);

        return new SlopeTileAccess(resultHeights, width, height);
    }

    @Override
    protected double getScaleFactorX(Coordinate regionSize) {
        return regionSize.getGlobalX() / regionSize.getBlockX();
    }

    @Override
    protected double getScaleFactorZ(Coordinate regionSize) {
        return regionSize.getGlobalZ() / regionSize.getBlockZ();
    }

    private byte[] scaleHeightRegion(byte[] sampledHeights, int sampleWidth, int width, int height, double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ) {
        byte[] resultHeights = new byte[width * height];

        for (int scaledZ = 0; scaledZ < height; scaledZ++) {
            double sampleZ = scaledZ * scaleFactorZ + originOffsetZ + LOWER_SAMPLE_BUFFER;
            int originZ = MathHelper.floor(sampleZ);
            double intermediateZ = sampleZ - originZ;

            for (int scaledX = 0; scaledX < width; scaledX++) {
                double sampleX = scaledX * scaleFactorX + originOffsetX + LOWER_SAMPLE_BUFFER;
                int originX = MathHelper.floor(sampleX);
                double intermediateX = sampleX - originX;

                byte heightValue = this.interpolatePoint(sampledHeights, sampleWidth, originX, originZ, intermediateX, intermediateZ);
                resultHeights[scaledX + scaledZ * width] = heightValue;
            }
        }

        return resultHeights;
    }

    private byte interpolatePoint(byte[] sampledHeights, int sampleWidth, int originX, int originZ, double intermediateX, double intermediateZ) {
        int sampleIndex = originX + originZ * sampleWidth;

        double current = sampledHeights[sampleIndex];
        double south = sampledHeights[sampleIndex + sampleWidth];
        double east = sampledHeights[sampleIndex + 1];
        double southEast = sampledHeights[sampleIndex + sampleWidth + 1];

        double y1 = Interpolation.linear(current, south, intermediateZ);
        double y2 = Interpolation.linear(east, southEast, intermediateZ);

        double interpolatedHeight = Interpolation.linear(y1, y2, intermediateX);

        return (byte) interpolatedHeight;
    }
}
