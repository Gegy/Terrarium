package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.util.Interpolate;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.FloatRaster;
import net.gegy1000.terrarium.server.world.data.raster.NumberRaster;
import net.gegy1000.terrarium.server.world.data.raster.ShortRaster;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public enum InterpolationScaleOp {
    NEAREST(Interpolate.NEAREST),
    LINEAR(Interpolate.LINEAR),
    COSINE(Interpolate.COSINE),
    CUBIC(Interpolate.CUBIC);

    private final Interpolate interpolate;
    private final ThreadLocal<double[][]> kernel2;
    private final ThreadLocal<double[]> kernel1;

    InterpolationScaleOp(Interpolate interpolate) {
        this.interpolate = interpolate;

        int kernelWidth = this.interpolate.getKernel().getWidth();
        this.kernel2 = ThreadLocal.withInitial(() -> new double[kernelWidth][kernelWidth]);
        this.kernel1 = ThreadLocal.withInitial(() -> new double[kernelWidth]);
    }

    public static InterpolationScaleOp appropriateForScale(double relativeScale) {
        if (relativeScale <= 1.0) {
            return InterpolationScaleOp.NEAREST;
        } else if (relativeScale <= 2.0) {
            return InterpolationScaleOp.LINEAR;
        } else if (relativeScale <= 3.0) {
            return InterpolationScaleOp.COSINE;
        }
        return InterpolationScaleOp.CUBIC;
    }

    public DataOp<ShortRaster> scaleShortsFrom(DataOp<ShortRaster> data, CoordinateReference src) {
        return this.scaleFrom(data, src, ShortRaster::create);
    }

    public DataOp<FloatRaster> scaleFloatsFrom(DataOp<FloatRaster> data, CoordinateReference src) {
        return this.scaleFrom(data, src, FloatRaster::create);
    }

    public <T extends NumberRaster<?>> DataOp<T> scaleFrom(DataOp<T> data, CoordinateReference src, Function<DataView, T> function) {
        return DataOp.of((view, executor) -> {
            DataView srcView = this.getSourceView(view, src);

            double destToSrcX = 1.0 / src.scaleX();
            double destToSrcY = 1.0 / src.scaleZ();

            Coordinate minCoordinate = Coordinate.min(
                    view.getMinCoordinate().to(src),
                    view.getMaxCoordinate().to(src)
            );

            double offsetX = minCoordinate.getX() - srcView.getMinX();
            double offsetY = minCoordinate.getZ() - srcView.getMinY();

            return data.apply(srcView, executor).andThen(opt -> {
                return executor.spawnBlocking(() -> opt.map(source -> {
                    double[][] kernel2 = this.kernel2.get();
                    double[] kernel1 = this.kernel1.get();
                    T result = function.apply(view);
                    for (int y = 0; y < view.getHeight(); y++) {
                        for (int x = 0; x < view.getWidth(); x++) {
                            double value = this.evaluate(kernel1, kernel2, source, x * destToSrcX + offsetX - 0.5, y * destToSrcY + offsetY - 0.5);
                            result.setDouble(x, y, value);
                        }
                    }
                    return result;
                }));
            });
        });
    }

    private <T extends NumberRaster<?>> double evaluate(double[] kernel1, double[][] kernel2, T source, double x, double y) {
        int originX = MathHelper.floor(x);
        int originY = MathHelper.floor(y);
        this.sampleKernel(source, kernel2, originX, originY);

        double intermediateX = x - originX;
        double intermediateY = y - originY;
        return this.interpolate.evaluate(kernel2, intermediateX, intermediateY, kernel1);
    }

    private <T extends NumberRaster<?>> void sampleKernel(T source, double[][] buffer, int x, int y) {
        Interpolate.Kernel kernel = this.interpolate.getKernel();
        int kernelWidth = kernel.getWidth();
        int kernelOffset = kernel.getOffset();

        for (int kernelY = 0; kernelY < kernelWidth; kernelY++) {
            int sourceY = y + kernelY + kernelOffset;
            for (int kernelX = 0; kernelX < kernelWidth; kernelX++) {
                int sourceX = x + kernelX + kernelOffset;
                buffer[kernelX][kernelY] = source.getDouble(sourceX, sourceY);
            }
        }
    }

    private DataView getSourceView(DataView view, CoordinateReference src) {
        Coordinate minBlockCoordinate = view.getMinCoordinate().to(src);
        Coordinate maxBlockCoordinate = view.getMaxCoordinate().to(src);

        Coordinate minCoordinate = Coordinate.min(minBlockCoordinate, maxBlockCoordinate);
        Coordinate maxCoordinate = Coordinate.max(minBlockCoordinate, maxBlockCoordinate);

        Interpolate.Kernel kernel = this.interpolate.getKernel();
        int kernelOffset = kernel.getOffset();
        int kernelWidth = kernel.getWidth();

        int minSampleX = MathHelper.floor(minCoordinate.getX() + kernelOffset - 0.5);
        int minSampleY = MathHelper.floor(minCoordinate.getZ() + kernelOffset - 0.5);

        int maxSampleX = MathHelper.floor(maxCoordinate.getX() + kernelOffset + kernelWidth - 0.5);
        int maxSampleY = MathHelper.floor(maxCoordinate.getZ() + kernelOffset + kernelWidth - 0.5);

        return DataView.fromCorners(minSampleX, minSampleY, maxSampleX, maxSampleY);
    }
}
