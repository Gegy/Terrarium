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
                    view.getMinCoordinate().to(src),
                    view.getMaxCoordinate().to(src)
            );

            float offsetX = (float) (minCoordinate.getX() - srcView.getX());
            float offsetY = (float) (minCoordinate.getZ() - srcView.getY());

            return data.apply(srcView, ctx).andThen(opt -> {
                return ctx.spawnBlocking(() -> {
                    return opt.map(source -> {
                        Profiler profiler = ThreadedProfiler.get();
                        try (Profiler.Handle scaleRaster = profiler.push("voronoi_raster")) {
                            EnumRaster<T> result = EnumRaster.create(defaultValue, view);
                            voronoi.scaleBytes(source.getData(), result.getData(), srcView, view, dstToSrcX, dstToSrcY, offsetX, offsetY);
                            return result;
                        }
                    });
                });
            });
        });
    }

    private static DataView getSourceView(DataView view, CoordinateReference src) {
        Coordinate minRegionCoordinateBlock = view.getMinCoordinate().to(src);
        Coordinate maxRegionCoordinateBlock = view.getMaxCoordinate().to(src);

        Coordinate minRegionCoordinate = Coordinate.min(minRegionCoordinateBlock, maxRegionCoordinateBlock);
        Coordinate maxRegionCoordinate = Coordinate.max(minRegionCoordinateBlock, maxRegionCoordinateBlock);

        int minSampleX = MathHelper.floor(minRegionCoordinate.getX()) - 1;
        int minSampleY = MathHelper.floor(minRegionCoordinate.getZ()) - 1;

        int maxSampleX = MathHelper.floor(maxRegionCoordinate.getX()) + 2;
        int maxSampleY = MathHelper.floor(maxRegionCoordinate.getZ()) + 2;

        return DataView.rect(minSampleX, minSampleY, maxSampleX - minSampleX, maxSampleY - minSampleY);
    }
}
