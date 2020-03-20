package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.terrarium.server.util.Vec2i;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.source.DataSourceReader;
import net.gegy1000.terrarium.server.world.data.source.DataTileResult;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class PolygonSampler {
    public static DataOp<PolygonData> sample(TiledDataSource<PolygonData> source, CoordinateReference crs) {
        return DataOp.of((view, executor) -> {
            Coordinate blockMin = view.getMinCoordinate().to(crs);
            Coordinate blockMax = view.getMaxCoordinate().to(crs);

            Coordinate min = Coordinate.min(blockMin, blockMax);
            Coordinate max = Coordinate.max(blockMin, blockMax);

            Vec2i minTilePos = getTilePos(source, min);
            Vec2i maxTilePos = getTilePos(source, max);

            return DataSourceReader.INSTANCE.getTiles(source, minTilePos, maxTilePos)
                    .andThen(tiles -> executor.spawnBlocking(() -> {
                        PolygonClipper clipper = PolygonClipper.rect(min.getX(), min.getZ(), max.getX(), max.getZ());

                        Collection<MultiPolygon> polygons = new ArrayList<>();

                        for (DataTileResult<PolygonData> entry : tiles) {
                            entry.data.ifPresent(data -> {
                                for (MultiPolygon polygon : data.getPolygons()) {
                                    MultiPolygon clipped = clipper.clip(polygon);
                                    if (clipped != null) {
                                        polygons.add(clipped);
                                    }
                                }
                            });
                        }

                        if (!polygons.isEmpty()) {
                            return Optional.of(new PolygonData(polygons));
                        } else {
                            return Optional.empty();
                        }
                    }));
        });
    }

    private static Vec2i getTilePos(TiledDataSource<PolygonData> source, Coordinate coordinate) {
        return new Vec2i(
                MathHelper.floor(coordinate.getX() / source.getTileWidth()),
                MathHelper.floor(coordinate.getZ() / source.getTileHeight())
        );
    }
}
