package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.util.InterpolationFunction;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.NumberRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public enum InterpolationScaleOp {
    NEAREST(InterpolationFunction.NEAREST),
    LINEAR(InterpolationFunction.LINEAR),
    COSINE(InterpolationFunction.COSINE),
    CUBIC(InterpolationFunction.CUBIC);

    private final InterpolationFunction function;
    private final ThreadLocal<double[][]> sampleBuffer;
    private final int lowerSampleBuffer;
    private final int upperSampleBuffer;

    InterpolationScaleOp(InterpolationFunction function) {
        this.function = function;

        int pointCount = function.getPointCount();
        this.sampleBuffer = ThreadLocal.withInitial(() -> new double[pointCount][pointCount]);

        this.lowerSampleBuffer = this.function.getBackward();
        this.upperSampleBuffer = this.function.getForward();
    }

    public DataOp<ShortRaster> scaleShortsFrom(DataOp<ShortRaster> data, CoordinateReference src) {
        return this.scaleFrom(data, src, ShortRaster::create);
    }

    public DataOp<FloatRaster> scaleFloatsFrom(DataOp<FloatRaster> data, CoordinateReference src) {
        return this.scaleFrom(data, src, FloatRaster::create);
    }

    public <T extends NumberRaster<?>> DataOp<T> scaleFrom(DataOp<T> data, CoordinateReference src, Function<DataView, T> function) {
        return DataOp.of(view -> {
            DataView srcView = this.getSourceView(view, src);

            double blockSizeX = view.getWidth();
            double blockSizeZ = view.getHeight();

            double scaleFactorX = Math.abs(src.x(blockSizeX, blockSizeZ) / blockSizeX);
            double scaleFactorZ = Math.abs(src.z(blockSizeX, blockSizeZ) / blockSizeZ);

            Coordinate minBlockCoordinate = view.getMinCoordinate().to(src);
            Coordinate maxBlockCoordinate = view.getMaxCoordinate().to(src);

            Coordinate minCoordinate = Coordinate.min(minBlockCoordinate, maxBlockCoordinate);

            double originOffsetX = minCoordinate.getX() - srcView.getX();
            double originOffsetZ = minCoordinate.getZ() - srcView.getY();

            double[][] sampleBuffer = this.sampleBuffer.get();

            return data.apply(srcView).thenApply(source -> {
                T result = function.apply(view);
                this.lerpRaster(sampleBuffer, source, result, originOffsetX, originOffsetZ, scaleFactorX, scaleFactorZ);
                return result;
            });
        });
    }

    private <T extends NumberRaster<?>> void lerpRaster(double[][] sampleBuffer, T source, T result, double offsetX, double offsetZ, double scaleFactorX, double scaleFactorZ) {
        int startX = 0;
        int startZ = 0;
        int endX = result.getWidth();
        int endZ = result.getHeight();

        int stepX = Integer.signum(endX - startX);
        int stepZ = Integer.signum(endZ - startZ);

        for (int scaledZ = startZ; scaledZ != endZ; scaledZ += stepZ) {
            double sampleZ = scaledZ * scaleFactorZ + offsetZ + this.lowerSampleBuffer;
            int originZ = MathHelper.floor(sampleZ);
            double intermediateZ = sampleZ - originZ;

            for (int scaledX = startX; scaledX != endX; scaledX += stepX) {
                double sampleX = scaledX * scaleFactorX + offsetX + this.lowerSampleBuffer;
                int originX = MathHelper.floor(sampleX);
                double intermediateX = sampleX - originX;

                double interpolatedValue = this.lerp(sampleBuffer, source, originX, originZ, intermediateX, intermediateZ);
                result.setDouble(scaledX, scaledZ, interpolatedValue);
            }
        }
    }

    private DataView getSourceView(DataView view, CoordinateReference src) {
        Coordinate minBlockCoordinate = view.getMinCoordinate().to(src);
        Coordinate maxBlockCoordinate = view.getMaxCoordinate().to(src);

        Coordinate minCoordinate = Coordinate.min(minBlockCoordinate, maxBlockCoordinate);
        Coordinate maxCoordinate = Coordinate.max(minBlockCoordinate, maxBlockCoordinate);

        int minSampleX = MathHelper.floor(minCoordinate.getX()) - this.lowerSampleBuffer;
        int minSampleY = MathHelper.floor(minCoordinate.getZ()) - this.lowerSampleBuffer;

        int maxSampleX = MathHelper.ceil(maxCoordinate.getX()) + this.upperSampleBuffer;
        int maxSampleY = MathHelper.ceil(maxCoordinate.getZ()) + this.upperSampleBuffer;

        return DataView.rect(minSampleX, minSampleY, maxSampleX - minSampleX + 1, maxSampleY - minSampleY + 1);
    }

    private <T extends NumberRaster<?>> double lerp(double[][] sampleBuffer, T source, int originX, int originZ, double intermediateX, double intermediateZ) {
        int backward = this.function.getBackward();
        int pointCount = this.function.getPointCount();
        for (int sampleZ = 0; sampleZ < pointCount; sampleZ++) {
            int globalZ = originZ + sampleZ - backward;
            for (int sampleX = 0; sampleX < pointCount; sampleX++) {
                int globalX = originX + sampleX - backward;
                sampleBuffer[sampleX][sampleZ] = source.getDouble(globalX, globalZ);
            }
        }

        return this.function.lerp2d(sampleBuffer, intermediateX, intermediateZ);
    }
}
