package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.util.InterpolationFunction;
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
    NEAREST(InterpolationFunction.NEAREST),
    LINEAR(InterpolationFunction.LINEAR),
    COSINE(InterpolationFunction.COSINE),
    CUBIC(InterpolationFunction.CUBIC);

    private final InterpolationFunction function;
    private final ThreadLocal<double[][]> sampleBuffer;
    private final int sampleOffset;
    private final int sampleWidth;

    InterpolationScaleOp(InterpolationFunction function) {
        this.function = function;

        this.sampleWidth = function.getSampleWidth();
        this.sampleBuffer = ThreadLocal.withInitial(() -> new double[this.sampleWidth][this.sampleWidth]);

        this.sampleOffset = this.function.getSampleOffset();
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

            double sizeX = view.getWidth();
            double sizeY = view.getHeight();

            double scaleX = Math.abs(src.x(sizeX, sizeY) / sizeX);
            double scaleY = Math.abs(src.z(sizeX, sizeY) / sizeY);

            Coordinate minCoordinate = Coordinate.min(
                    view.getMinCoordinate().to(src),
                    view.getMaxCoordinate().to(src)
            );

            double offsetX = minCoordinate.getX() - MathHelper.floor(minCoordinate.getX());
            double offsetY = minCoordinate.getZ() - MathHelper.floor(minCoordinate.getZ());

            return data.apply(srcView).thenApply(source -> {
                double[][] sampleBuffer = this.sampleBuffer.get();
                T result = function.apply(view);
                for (int y = 0; y < view.getHeight(); y++) {
                    for (int x = 0; x < view.getWidth(); x++) {
                        double value = this.lerp(sampleBuffer, source, x * scaleX + offsetX, y * scaleY + offsetY);
                        result.setDouble(x, y, value);
                    }
                }
                return result;
            });
        });
    }

    private DataView getSourceView(DataView view, CoordinateReference src) {
        Coordinate minBlockCoordinate = view.getMinCoordinate().to(src);
        Coordinate maxBlockCoordinate = view.getMaxCoordinate().to(src);

        Coordinate minCoordinate = Coordinate.min(minBlockCoordinate, maxBlockCoordinate);
        Coordinate maxCoordinate = Coordinate.max(minBlockCoordinate, maxBlockCoordinate);

        int minSampleX = MathHelper.floor(minCoordinate.getX()) + this.sampleOffset;
        int minSampleY = MathHelper.floor(minCoordinate.getZ()) + this.sampleOffset;

        int maxSampleX = MathHelper.ceil(maxCoordinate.getX()) + this.sampleOffset + this.sampleWidth;
        int maxSampleY = MathHelper.ceil(maxCoordinate.getZ()) + this.sampleOffset + this.sampleWidth;

        return DataView.fromCorners(minSampleX, minSampleY, maxSampleX, maxSampleY);
    }

    private <T extends NumberRaster<?>> double lerp(double[][] sampleBuffer, T source, double x, double y) {
        int originX = MathHelper.floor(x) + this.sampleOffset;
        int originY = MathHelper.floor(y) + this.sampleOffset;

        double intermediateX = x - originX;
        double intermediateY = y - originY;

        for (int sampleY = 0; sampleY < this.sampleWidth; sampleY++) {
            int sourceY = originY + (sampleY - this.sampleOffset);
            for (int sampleX = 0; sampleX < this.sampleWidth; sampleX++) {
                int sourceX = originX + (sampleX - this.sampleOffset);
                sampleBuffer[sampleX][sampleY] = source.getDouble(sourceX, sourceY);
            }
        }

        return this.function.lerp2d(sampleBuffer, intermediateX, intermediateY);
    }
}
