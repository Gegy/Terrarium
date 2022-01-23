package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.data.source.DataSourceReader;
import net.gegy1000.terrarium.server.world.data.source.DataTileResult;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public final class PolygonSampler {
    public static DataOp<PolygonData> sample(TiledDataSource<PolygonData> source, CoordinateReference crs, double sampleExpand) {
        return DataOp.of((view, ctx) -> {
            Coordinate blockMin = view.minCoordinate()
                    .addLocal(-sampleExpand, -sampleExpand)
                    .to(crs);
            Coordinate blockMax = view.maxCoordinate()
                    .addLocal(sampleExpand, sampleExpand)
                    .to(crs);

            Coordinate min = Coordinate.min(blockMin, blockMax);
            Coordinate max = Coordinate.max(blockMin, blockMax);

            return DataSourceReader.INSTANCE.getTilesIntersecting(source, min.x(), min.z(), max.x(), max.z())
                    .andThen(tiles -> ctx.spawnBlocking(() -> {
                        Collection<MultiPolygon> polygons = clipPolygons(min, max, tiles);
                        if (!polygons.isEmpty()) {
                            return Optional.of(new PolygonData(polygons));
                        } else {
                            return Optional.empty();
                        }
                    }));
        });
    }

    private static Collection<MultiPolygon> clipPolygons(Coordinate min, Coordinate max, Collection<DataTileResult<PolygonData>> tiles) {
        Profiler profiler = ThreadedProfiler.get();

        try (Profiler.Handle clipPolygons = profiler.push("clip_polygons")) {
            Collection<MultiPolygon> polygons = new ArrayList<>();
            PolygonClipper clipper = PolygonClipper.rect(min.x(), min.z(), max.x(), max.z());

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

            return polygons;
        }
    }
}
