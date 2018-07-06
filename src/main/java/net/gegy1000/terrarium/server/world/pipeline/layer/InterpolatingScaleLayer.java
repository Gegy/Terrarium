package net.gegy1000.terrarium.server.world.pipeline.layer;

import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.NumberRasterTile;
import net.minecraft.util.math.MathHelper;

public abstract class InterpolatingScaleLayer<T extends NumberRasterTile<?>> extends BufferedScalingLayer<T> {
    private final Interpolation.Method interpolationMethod;
    private final double[][] sampleBuffer;

    public InterpolatingScaleLayer(Interpolation.Method interpolationMethod, CoordinateState coordinateState) {
        super(interpolationMethod.getBackward(), interpolationMethod.getForward() + 1, coordinateState);

        this.interpolationMethod = interpolationMethod;
        int pointCount = interpolationMethod.getPointCount();

        this.sampleBuffer = new double[pointCount][pointCount];
    }

    protected final void scaleRegion(T sampled, T result, double scaleFactorX, double scaleFactorZ, double originOffsetX, double originOffsetZ) {
        int startX = 0;
        int startZ = 0;
        int endX = result.getWidth();
        int endZ = result.getHeight();

        if (scaleFactorX < 0.0) {
            scaleFactorX = -scaleFactorX;
        }

        if (scaleFactorZ < 0.0) {
            scaleFactorZ = -scaleFactorZ;
        }

        int stepX = Integer.signum(endX - startX);
        int stepZ = Integer.signum(endZ - startZ);

        for (int scaledZ = startZ; scaledZ != endZ; scaledZ += stepZ) {
            double sampleZ = scaledZ * scaleFactorZ + originOffsetZ + this.lowerSampleBuffer;
            int originZ = MathHelper.floor(sampleZ);
            double intermediateZ = sampleZ - originZ;

            for (int scaledX = startX; scaledX != endX; scaledX += stepX) {
                double sampleX = scaledX * scaleFactorX + originOffsetX + this.lowerSampleBuffer;
                int originX = MathHelper.floor(sampleX);
                double intermediateX = sampleX - originX;

                double heightValue = this.interpolatePoint(sampled, originX, originZ, intermediateX, intermediateZ);
                result.setDouble(scaledX, scaledZ, heightValue);
            }
        }
    }

    private double interpolatePoint(T sampled, int originX, int originZ, double intermediateX, double intermediateZ) {
        int backward = this.interpolationMethod.getBackward();
        int pointCount = this.interpolationMethod.getPointCount();
        for (int sampleZ = 0; sampleZ < pointCount; sampleZ++) {
            int globalZ = originZ + sampleZ - backward;
            for (int sampleX = 0; sampleX < pointCount; sampleX++) {
                int globalX = originX + sampleX - backward;
                this.sampleBuffer[sampleX][sampleZ] = sampled.getDouble(globalX, globalZ);
            }
        }

        return (short) this.interpolationMethod.lerp2d(this.sampleBuffer, intermediateX, intermediateZ);
    }
}
