package net.gegy1000.terrarium.server.map.system.populator;

import net.gegy1000.terrarium.server.map.RegionTilePos;
import net.gegy1000.terrarium.server.map.source.height.HeightTileAccess;
import net.gegy1000.terrarium.server.map.system.sampler.DataSampler;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

public class HeightRegionPopulator extends BufferedScalingPopulator<HeightTileAccess> {
    private static final int LOWER_SAMPLE_BUFFER = 0;
    private static final int UPPER_SAMPLE_BUFFER = 1;

    private final DataSampler<short[]> heightSampler;

    private final Interpolation.Method interpolationMethod;
    private final double[][] sampleBuffer;

    public HeightRegionPopulator(DataSampler<short[]> heightSampler, Interpolation.Method interpolationMethod) {
        super(LOWER_SAMPLE_BUFFER + interpolationMethod.getBackward(), UPPER_SAMPLE_BUFFER + interpolationMethod.getForward(), Coordinate::getGlobalX, Coordinate::getGlobalZ);
        this.heightSampler = heightSampler;

        this.interpolationMethod = interpolationMethod;

        int pointCount = interpolationMethod.getPointCount();
        this.sampleBuffer = new double[pointCount][pointCount];
    }

    @Override
    protected HeightTileAccess populate(EarthGenerationSettings settings, RegionTilePos pos, int minSampleX, int minSampleZ,
                                        int sampleWidth, int sampleHeight, int width, int height,
                                        double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ
    ) {
        short[] sampledHeights = this.heightSampler.sample(settings, minSampleX, minSampleZ, sampleWidth, sampleHeight);
        short[] resultHeights = this.scaleHeightRegion(sampledHeights, sampleWidth, width, height, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);

        return new HeightTileAccess(resultHeights, width, height);
    }

    @Override
    protected double getScaleFactorX(Coordinate regionSize) {
        return regionSize.getGlobalX() / regionSize.getBlockX();
    }

    @Override
    protected double getScaleFactorZ(Coordinate regionSize) {
        return regionSize.getGlobalZ() / regionSize.getBlockZ();
    }

    private short[] scaleHeightRegion(short[] sampledHeights, int sampleWidth, int width, int height, double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ) {
        short[] resultHeights = new short[width * height];

        for (int scaledZ = 0; scaledZ < height; scaledZ++) {
            double sampleZ = scaledZ * scaleFactorZ + originOffsetZ + this.lowerSampleBuffer;
            int originZ = MathHelper.floor(sampleZ);
            double intermediateZ = sampleZ - originZ;

            for (int scaledX = 0; scaledX < width; scaledX++) {
                double sampleX = scaledX * scaleFactorX + originOffsetX + this.lowerSampleBuffer;
                int originX = MathHelper.floor(sampleX);
                double intermediateX = sampleX - originX;

                short heightValue = this.interpolatePoint(sampledHeights, sampleWidth, originX, originZ, intermediateX, intermediateZ);
                resultHeights[scaledX + scaledZ * width] = heightValue;
            }
        }

        return resultHeights;
    }

    private short interpolatePoint(short[] sampledHeights, int sampleWidth, int originX, int originZ, double intermediateX, double intermediateZ) {
        int baseIndex = originX + originZ * sampleWidth;

        int backward = this.interpolationMethod.getBackward();
        int pointCount = this.interpolationMethod.getPointCount();
        for (int sampleZ = 0; sampleZ < pointCount; sampleZ++) {
            for (int sampleX = 0; sampleX < pointCount; sampleX++) {
                int sampleIndex = baseIndex + (sampleX - backward) + (sampleZ - backward) * sampleWidth;
                this.sampleBuffer[sampleX][sampleZ] = sampledHeights[sampleIndex];
            }
        }

        return (short) this.interpolationMethod.lerp2d(this.sampleBuffer, intermediateX, intermediateZ);
    }
}
