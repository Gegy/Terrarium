package net.gegy1000.earth.server.world.data.op;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.op.InterpolationScaleOp;
import net.gegy1000.terrarium.server.world.data.raster.NumberRaster;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class ResampleZoomRasters<T extends NumberRaster<?>> {
    private Zoomable<? extends TiledDataSource<T>> zoomableSource;
    private int standardZoom;

    private Function<? super TiledDataSource<T>, DataOp<T>> sampleFunction;

    public ResampleZoomRasters<T> from(Zoomable<? extends TiledDataSource<T>> zoomableSource) {
        this.zoomableSource = zoomableSource;
        return this;
    }

    public ResampleZoomRasters<T> sample(Function<? super TiledDataSource<T>, DataOp<T>> function) {
        this.sampleFunction = function;
        return this;
    }

    public ResampleZoomRasters<T> atStandardZoom(int standardZoom) {
        this.standardZoom = standardZoom;
        return this;
    }

    public DataOp<T> create(Function<DataView, T> createRaster) {
        Preconditions.checkNotNull(this.zoomableSource, "source not set");
        Preconditions.checkNotNull(this.sampleFunction, "sample function not set");
        return this.resampleRecursively(this.standardZoom, createRaster);
    }

    private DataOp<T> resampleRecursively(int zoom, Function<DataView, T> createRaster) {
        DataOp<T> sampleOp = this.sampleAndScaleAtZoom(zoom, createRaster);
        return DataOp.of(view -> {
            return sampleOp.apply(view).thenCompose(result -> {
                int nextZoom = zoom - 1;
                if (result.isPresent() || !this.zoomableSource.contains(nextZoom)) {
                    return CompletableFuture.completedFuture(result);
                }

                return this.resampleRecursively(nextZoom, createRaster).apply(view);
            });
        });
    }

    private DataOp<T> sampleAndScaleAtZoom(int zoom, Function<DataView, T> createRaster) {
        TiledDataSource<T> source = this.zoomableSource.forZoom(zoom);
        if (source == null) return DataOp.completed(Optional.empty());

        DataOp<T> sample = this.sampleFunction.apply(source);

        CoordinateReference crs = source.getCrs();
        double avgScale = (Math.abs(crs.scaleX()) + Math.abs(crs.scaleZ())) / 2.0;

        return InterpolationScaleOp.appropriateForScale(avgScale)
                .scaleFrom(sample, crs, createRaster);
    }
}
