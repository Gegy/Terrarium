package net.gegy1000.terrarium.server.world.data.op;

import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.util.Voronoi;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.gegy1000.terrarium.server.world.data.raster.EnumRaster;
import net.minecraft.util.math.MathHelper;

public final class VoronoiScaleOp {
    private static final long SEED = 2016969737595986194L;

    public static <T extends Enum<T>> DataOp<EnumRaster<T>> scaleEnumsFrom(DataOp<EnumRaster<T>> data, CoordinateReference src, T defaultValue) {
        Voronoi voronoi = new Voronoi(0.45F, SEED);

        return DataOp.of((view, ctx) -> {
            DataView srcView = getSourceView(view, src);

            float dstToSrcX = (float) (1.0 / src.scaleX());
            float dstToSrcY = (float) (1.0 / src.scaleZ());

            Coordinate minCoordinate = Coordinate.min(
                    view.minCoordinate().to(src),
                    view.maxCoordinate().to(src)
            );

            float offsetX = (float) (minCoordinate.x() - srcView.minX());
            float offsetY = (float) (minCoordinate.z() - srcView.minY());

            return data.apply(srcView, ctx).andThen(opt -> {
                return ctx.spawnBlocking(() -> {
                    return opt.map(source -> {
                        Profiler profiler = ThreadedProfiler.get();
                        try (Profiler.Handle scaleRaster = profiler.push("voronoi_raster")) {
                            EnumRaster<T> result = EnumRaster.create(defaultValue, view);
                            voronoi.scaleBytes(source.asRawData(), result.asRawData(), srcView, view, dstToSrcX, dstToSrcY, offsetX, offsetY);
                            return result;
                        }
                    });
                });
            });
        });
    }

    private static DataView getSourceView(DataView view, CoordinateReference src) {
        Coordinate minSourceBlock = view.minCoordinate().to(src);
        Coordinate maxSourceBlock = view.maxCoordinate().to(src);

        Coordinate minSource = Coordinate.min(minSourceBlock, maxSourceBlock);
        Coordinate maxSource = Coordinate.max(minSourceBlock, maxSourceBlock);

        int minSourceX = MathHelper.floor(minSource.x()) - 1;
        int minSourceY = MathHelper.floor(minSource.z()) - 1;

        int maxSourceX = MathHelper.floor(maxSource.x()) + 1;
        int maxSourceY = MathHelper.floor(maxSource.z()) + 1;

        return DataView.ofCorners(minSourceX, minSourceY, maxSourceX, maxSourceY);
    }
}
