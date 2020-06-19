package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.MultiPolygon;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.rasterization.PolygonShapeProducer;

import java.awt.geom.Area;

public final class PolygonToLocalAreaOp {
    public static DataOp<Area> apply(DataOp<PolygonData> polygons, CoordinateReference crs) {
        return polygons.mapBlocking((polygonData, view) -> {
            PolygonShapeProducer.Transform transform = new PolygonShapeProducer.Transform() {
                @Override
                public double x(double x) {
                    return crs.blockX(x) - view.getMinX();
                }

                @Override
                public double y(double y) {
                    return crs.blockZ(y) - view.getMinY();
                }
            };

            Profiler profiler = ThreadedProfiler.get();
            try (Profiler.Handle polygonToArea = profiler.push("polygon_to_area")) {
                Area area = new Area();
                for (MultiPolygon polygon : polygonData.getPolygons()) {
                    area.add(PolygonShapeProducer.toShape(polygon, transform));
                }
                return area;
            }
        });
    }
}
