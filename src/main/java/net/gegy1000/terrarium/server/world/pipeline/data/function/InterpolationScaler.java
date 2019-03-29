package net.gegy1000.terrarium.server.world.pipeline.data.function;

import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.DataFuture;
import net.gegy1000.terrarium.server.world.pipeline.data.RasterConstructor;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.NumberRaster;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.util.math.MathHelper;

public enum InterpolationScaler {
    LINEAR(Interpolation.Method.LINEAR),
    COSINE(Interpolation.Method.COSINE),
    CUBIC(Interpolation.Method.CUBIC);

    private final Interpolation.Method method;
    private final double[][] sampleBuffer;
    private final int lowerSampleBuffer;
    private final int upperSampleBuffer;

    InterpolationScaler(Interpolation.Method method) {
        this.method = method;

        int pointCount = method.getPointCount();
        this.sampleBuffer = new double[pointCount][pointCount];

        this.lowerSampleBuffer = this.method.getBackward();
        this.upperSampleBuffer = this.method.getForward();
    }

    public DataFuture<ShortRaster> scaleShortFrom(DataFuture<ShortRaster> data, CoordinateState src) {
        return this.scaleFrom(data, src, ShortRaster::new);
    }

    public DataFuture<FloatRaster> scaleFloatFrom(DataFuture<FloatRaster> data, CoordinateState src) {
        return this.scaleFrom(data, src, FloatRaster::new);
    }

    public <T extends NumberRaster<?>> DataFuture<T> scaleFrom(DataFuture<T> data, CoordinateState src, RasterConstructor<T> constructor) {
        return DataFuture.of((engine, view) -> {
            DataView srcView = this.getSourceView(view, src);

            double blockSizeX = view.getWidth();
            double blockSizeZ = view.getHeight();

            double scaleFactorX = Math.abs(src.getX(blockSizeX, blockSizeZ) / blockSizeX);
            double scaleFactorZ = Math.abs(src.getZ(blockSizeX, blockSizeZ) / blockSizeZ);

            Coordinate minRegionCoordinateBlock = view.getMinCoordinate().to(src);
            Coordinate maxRegionCoordinateBlock = view.getMaxCoordinate().to(src);

            Coordinate minRegionCoordinate = Coordinate.min(minRegionCoordinateBlock, maxRegionCoordinateBlock);

            double originOffsetX = minRegionCoordinate.getX() - srcView.getX();
            double originOffsetZ = minRegionCoordinate.getZ() - srcView.getY();

            return engine.load(data, srcView).thenApply(source -> {
                T result = constructor.construct(view);
                this.lerpRaster(source, result, originOffsetX, originOffsetZ, scaleFactorX, scaleFactorZ);
                return result;
            });
        });
    }

    private <T extends NumberRaster<?>> void lerpRaster(T source, T result, double offsetX, double offsetZ, double scaleFactorX, double scaleFactorZ) {
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

                double interpolatedValue = this.lerp(source, originX, originZ, intermediateX, intermediateZ);
                result.setDouble(scaledX, scaledZ, interpolatedValue);
            }
        }
    }

    private DataView getSourceView(DataView view, CoordinateState src) {
        Coordinate minRegionCoordinateBlock = view.getMinCoordinate().to(src);
        Coordinate maxRegionCoordinateBlock = view.getMaxCoordinate().to(src);

        Coordinate minRegionCoordinate = Coordinate.min(minRegionCoordinateBlock, maxRegionCoordinateBlock);
        Coordinate maxRegionCoordinate = Coordinate.max(minRegionCoordinateBlock, maxRegionCoordinateBlock);

        int minSampleX = MathHelper.floor(minRegionCoordinate.getX()) - this.lowerSampleBuffer;
        int minSampleY = MathHelper.floor(minRegionCoordinate.getZ()) - this.lowerSampleBuffer;

        int maxSampleX = MathHelper.ceil(maxRegionCoordinate.getX()) + this.upperSampleBuffer;
        int maxSampleY = MathHelper.ceil(maxRegionCoordinate.getZ()) + this.upperSampleBuffer;

        return new DataView(minSampleX, minSampleY, maxSampleX - minSampleX + 1, maxSampleY - minSampleY + 1);
    }

    private <T extends NumberRaster<?>> double lerp(T source, int originX, int originZ, double intermediateX, double intermediateZ) {
        int backward = this.method.getBackward();
        int pointCount = this.method.getPointCount();
        for (int sampleZ = 0; sampleZ < pointCount; sampleZ++) {
            int globalZ = originZ + sampleZ - backward;
            for (int sampleX = 0; sampleX < pointCount; sampleX++) {
                int globalX = originX + sampleX - backward;
                this.sampleBuffer[sampleX][sampleZ] = source.getDouble(globalX, globalZ);
            }
        }

        return this.method.lerp2d(this.sampleBuffer, intermediateX, intermediateZ);
    }
}
