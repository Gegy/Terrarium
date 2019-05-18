package net.gegy1000.terrarium.server.world.pipeline.data.function;

import net.gegy1000.terrarium.server.util.Voronoi;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.Raster;
import net.minecraft.util.math.MathHelper;

import java.util.function.Function;

public final class VoronoiScaleOp {
    public static <V, T extends Raster<V[]>> DataOp<T> scaleFrom(DataOp<T> data, CoordinateState src, Function<DataView, T> function) {
        Voronoi voronoi = new Voronoi(Voronoi.DistanceFunc.EUCLIDEAN, 0.9, 4, 1000);
        return DataOp.of((engine, view) -> {
            DataView srcView = getSourceView(view, src);

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
                T result = function.apply(view);
                voronoi.scale(source.getData(), result.getData(), srcView, view, scaleFactorX, scaleFactorZ, originOffsetX, originOffsetZ);
                return result;
            });
        });
    }

    private static DataView getSourceView(DataView view, CoordinateState src) {
        Coordinate minRegionCoordinateBlock = view.getMinCoordinate().to(src);
        Coordinate maxRegionCoordinateBlock = view.getMaxCoordinate().to(src);

        Coordinate minRegionCoordinate = Coordinate.min(minRegionCoordinateBlock, maxRegionCoordinateBlock);
        Coordinate maxRegionCoordinate = Coordinate.max(minRegionCoordinateBlock, maxRegionCoordinateBlock);

        int minSampleX = MathHelper.floor(minRegionCoordinate.getX()) - 1;
        int minSampleY = MathHelper.floor(minRegionCoordinate.getZ()) - 1;

        int maxSampleX = MathHelper.ceil(maxRegionCoordinate.getX()) + 2;
        int maxSampleY = MathHelper.ceil(maxRegionCoordinate.getZ()) + 2;

        return DataView.rect(minSampleX, minSampleY, maxSampleX - minSampleX, maxSampleY - minSampleY);
    }
}
