package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTileEntry;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;

public final class PolygonSampler {
    public static DataOp<PolygonData> sample(TiledDataSource<PolygonData> source, CoordinateState coordinateState) {
        return DataOp.of((engine, view) -> {
            DataSourceHandler sourceHandler = engine.getSourceHandler();

            Coordinate blockMin = view.getMinCoordinate().to(coordinateState);
            Coordinate blockMax = view.getMaxCoordinate().to(coordinateState);

            Coordinate min = Coordinate.min(blockMin, blockMax);
            Coordinate max = Coordinate.max(blockMin, blockMax);

            DataTilePos minTilePos = getTilePos(source, min);
            DataTilePos maxTilePos = getTilePos(source, max);

            return sourceHandler.getTiles(source, minTilePos, maxTilePos)
                    .thenApply(tiles -> {
                        PolygonClipper clipper = new PolygonClipper(min.getX(), min.getZ(), max.getX(), max.getZ());

                        Collection<MultiPolygon> polygons = new ArrayList<>();

                        for (DataTileEntry<PolygonData> entry : tiles) {
                            PolygonData data = entry.getData();

                            for (MultiPolygon polygon : data.getPolygons()) {
                                MultiPolygon clipped = clipper.clip(polygon);
                                if (clipped != null) {
                                    polygons.add(clipped);
                                }
                            }
                        }

                        return new PolygonData(polygons);
                    });
        });
    }

    private static DataTilePos getTilePos(TiledDataSource<PolygonData> source, Coordinate coordinate) {
        Coordinate tileSize = source.getTileSize();
        int tileX = MathHelper.floor(coordinate.getX() / tileSize.getX());
        int tileZ = MathHelper.floor(coordinate.getZ() / tileSize.getZ());
        return new DataTilePos(tileX, tileZ);
    }
}
