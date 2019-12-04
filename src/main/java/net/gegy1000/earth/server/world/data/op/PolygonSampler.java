package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.data.source.DataTileEntry;
import net.gegy1000.terrarium.server.world.data.source.DataTilePos;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;

public final class PolygonSampler {
    public static DataOp<PolygonData> sample(TiledDataSource<PolygonData> source, CoordinateReference coordinateReference) {
        return DataOp.of(view -> {
            Coordinate blockMin = view.getMinCoordinate().to(coordinateReference);
            Coordinate blockMax = view.getMaxCoordinate().to(coordinateReference);

            Coordinate min = Coordinate.min(blockMin, blockMax);
            Coordinate max = Coordinate.max(blockMin, blockMax);

            DataTilePos minTilePos = getTilePos(source, min);
            DataTilePos maxTilePos = getTilePos(source, max);

            return DataSourceHandler.INSTANCE.getTiles(source, minTilePos, maxTilePos)
                    .thenApply(tiles -> {
                        PolygonClipper clipper = PolygonClipper.rect(min.getX(), min.getZ(), max.getX(), max.getZ());

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
