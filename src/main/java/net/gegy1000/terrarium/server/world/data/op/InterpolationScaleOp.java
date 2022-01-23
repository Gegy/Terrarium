package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.util.Interpolate;
import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
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
        return DataOp.of((view, ctx) -> {
            DataView srcView = this.getSourceView(view, src);

            double dstToSrcX = 1.0 / src.scaleX();
            double dstToSrcY = 1.0 / src.scaleZ();

            Coordinate minCoordinate = Coordinate.min(
                    view.minCoordinate().to(src),
                    view.maxCoordinate().to(src)
            );

            double offsetX = minCoordinate.x() - srcView.minX();
            double offsetY = minCoordinate.z() - srcView.minY();

            return data.apply(srcView, ctx).andThen(opt -> {
                return ctx.spawnBlocking(() -> opt.map(source -> {
                    T result = function.apply(view);
                    Profiler profiler = ThreadedProfiler.get();
                    try (Profiler.Handle scaleRaster = profiler.push("interpolate_raster")) {
                        if (this == NEAREST) {
                            this.scaleIntoNearest(source, result, dstToSrcX, dstToSrcY, offsetX, offsetY);
                        } else {
                            this.scaleInto(source, result, dstToSrcX, dstToSrcY, offsetX, offsetY);
                        }
                        return result;
                    }
                }));
            });
        });
    }

    private <T extends NumberRaster<?>> void scaleInto(T src, T dst, double dstToSrcX, double dstToSrcY, double offsetX, double offsetY) {
        double[][] kernel2 = this.kernel2.get();
        double[] kernel1 = this.kernel1.get();
        for (int y = 0; y < dst.height(); y++) {
            for (int x = 0; x < dst.width(); x++) {
                float value = this.evaluate(
                        kernel1, kernel2, src,
                        x * dstToSrcX + offsetX - 0.5,
                        y * dstToSrcY + offsetY - 0.5
                );
                dst.setFloat(x, y, value);
            }
        }
    }

    private <T extends NumberRaster<?>> void scaleIntoNearest(T src, T dst, double dstToSrcX, double dstToSrcY, double offsetX, double offsetY) {
        for (int y = 0; y < dst.height(); y++) {
            for (int x = 0; x < dst.width(); x++) {
                int srcX = MathHelper.floor(x * dstToSrcX + offsetX - 0.5);
                int srcY = MathHelper.floor(y * dstToSrcY + offsetY - 0.5);
                dst.setFloat(x, y, src.getFloat(srcX, srcY));
            }
        }
    }

    private <T extends NumberRaster<?>> float evaluate(double[] kernel1, double[][] kernel2, T source, double x, double y) {
        int originX = MathHelper.floor(x);
        int originY = MathHelper.floor(y);
        this.sampleKernel(source, kernel2, originX, originY);

        double intermediateX = x - originX;
        double intermediateY = y - originY;
        return (float) this.interpolate.evaluate(kernel2, intermediateX, intermediateY, kernel1);
    }

    private <T extends NumberRaster<?>> void sampleKernel(T source, double[][] buffer, int x, int y) {
        Interpolate.Kernel kernel = this.interpolate.getKernel();
        int kernelWidth = kernel.getWidth();
        int kernelOffset = kernel.getOffset();

        for (int kernelY = 0; kernelY < kernelWidth; kernelY++) {
            int sourceY = y + kernelY + kernelOffset;
            for (int kernelX = 0; kernelX < kernelWidth; kernelX++) {
                int sourceX = x + kernelX + kernelOffset;
                buffer[kernelX][kernelY] = source.getFloat(sourceX, sourceY);
            }
        }
    }

    private DataView getSourceView(DataView view, CoordinateReference crs) {
        Coordinate minSourceBlock = view.minCoordinate().to(crs);
        Coordinate maxSourceBlock = view.maxCoordinate().to(crs);

        Coordinate minSource = Coordinate.min(minSourceBlock, maxSourceBlock);
        Coordinate maxSource = Coordinate.max(minSourceBlock, maxSourceBlock);

        Interpolate.Kernel kernel = this.interpolate.getKernel();
        int kernelOffset = kernel.getOffset();
        int kernelWidth = kernel.getWidth();

        int minSourceX = MathHelper.floor(minSource.x() + kernelOffset - 0.5);
        int minSourceY = MathHelper.floor(minSource.z() + kernelOffset - 0.5);

        int maxSourceX = MathHelper.floor(maxSource.x() + kernelOffset + kernelWidth - 0.5);
        int maxSourceY = MathHelper.floor(maxSource.z() + kernelOffset + kernelWidth - 0.5);

        return DataView.ofCorners(minSourceX, minSourceY, maxSourceX, maxSourceY);
    }
}
