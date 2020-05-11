package net.gegy1000.earth.server.world.data.op;

import com.google.common.base.Preconditions;
import net.gegy1000.earth.server.util.zoom.ZoomLevels;
import net.gegy1000.earth.server.util.zoom.Zoomable;
import net.gegy1000.terrarium.server.world.coordinate.CoordReferenced;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ResampleZoomRasters<T> {
    private Zoomable<CoordReferenced<? extends TiledDataSource<T>>> zoomableSource;
    private int zoom;

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

    public ResampleZoomRasters<T> atZoom(int zoom) {
        this.zoom = zoom;
        return this;
    }

    public DataOp<T> create() {
        Preconditions.checkNotNull(this.zoomableSource, "source not set");
        Preconditions.checkNotNull(this.sampleFunction, "sample function not set");
        Preconditions.checkNotNull(this.resampleFunction, "resample function not set");

        ZoomLevels levels = this.zoomableSource.getLevels();
        int zoom = MathHelper.clamp(this.zoom, levels.min, levels.max);

        CoordReferenced<? extends TiledDataSource<T>> referencedSource = this.zoomableSource.forZoom(zoom);
        if (referencedSource == null) return DataOp.ready(Optional.empty());

        DataOp<T> sample = this.sampleFunction.apply(referencedSource.source);
        CoordinateReference crs = referencedSource.crs;

        return this.resampleFunction.apply(sample, crs);
    }
}
