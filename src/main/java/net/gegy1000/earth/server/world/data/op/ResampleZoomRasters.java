package net.gegy1000.earth.server.world.data.op;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.server.world.coordinate.CoordReferenced;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ResampleZoomRasters<T> {
    private Zoomable<CoordReferenced<? extends TiledDataSource<T>>> zoomableSource;
    private int maxZoom;

    private Function<? super TiledDataSource<T>, DataOp<T>> sampleFunction;
    private BiFunction<DataOp<T>, CoordinateReference, DataOp<T>> resampleFunction;

    public ResampleZoomRasters<T> from(Zoomable<CoordReferenced<? extends TiledDataSource<T>>> source) {
        this.zoomableSource = source;
        return this;
    }

    public ResampleZoomRasters<T> sample(Function<? super TiledDataSource<T>, DataOp<T>> function) {
        this.sampleFunction = function;
        return this;
    }

    public ResampleZoomRasters<T> resample(BiFunction<DataOp<T>, CoordinateReference, DataOp<T>> function) {
        this.resampleFunction = function;
        return this;
    }

    public ResampleZoomRasters<T> atZoom(int maxZoom) {
        this.maxZoom = maxZoom;
        return this;
    }

    public DataOp<T> create() {
        Preconditions.checkNotNull(this.zoomableSource, "source not set");
        Preconditions.checkNotNull(this.sampleFunction, "sample function not set");
        Preconditions.checkNotNull(this.resampleFunction, "resample function not set");

        int upperZoom = Math.min(this.maxZoom, this.zoomableSource.getLevels().max);
        return this.resampleRecursively(upperZoom);
    }

    // TODO: provide global coverage at all zoom levels
    private DataOp<T> resampleRecursively(int zoom) {
        DataOp<T> sampleOp = this.sampleAndScaleAtZoom(zoom);
        return DataOp.of((view, executor) -> {
            return sampleOp.apply(view, executor).andThen(result -> {
                int nextZoom = zoom - 1;
                if (result.isPresent() || !this.zoomableSource.contains(nextZoom)) {
                    return Future.ready(result);
                }

                return this.resampleRecursively(nextZoom).apply(view, executor);
            });
        });
    }

    private DataOp<T> sampleAndScaleAtZoom(int zoom) {
        CoordReferenced<? extends TiledDataSource<T>> referencedSource = this.zoomableSource.forZoom(zoom);
        if (referencedSource == null) return DataOp.ready(Optional.empty());

        DataOp<T> sample = this.sampleFunction.apply(referencedSource.source);
        CoordinateReference crs = referencedSource.crs;

        return this.resampleFunction.apply(sample, crs);
    }
}
