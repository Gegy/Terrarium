package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import net.gegy1000.earth.server.world.data.PolygonData;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateReference;
import net.gegy1000.terrarium.server.world.data.DataOp;
import net.gegy1000.terrarium.server.world.rasterization.PolygonShapeProducer;

import java.awt.geom.Area;

public final class PolygonToAreaOp {
    public static DataOp<Area> apply(DataOp<PolygonData> polygons, CoordinateReference crs) {
        return polygons.mapBlocking((polygonData, view) -> {
            Area area = new Area();

            for (MultiPolygon polygon : polygonData.getPolygons()) {
                for (int i = 0; i < polygon.getNumGeometries(); i++) {
                    Geometry geometry = polygon.getGeometryN(i);
                    if (geometry instanceof Polygon) {
                        area.add(PolygonShapeProducer.toShape((Polygon) geometry, crs));
                    }
                }
            }

            return area;
        });
    }
}
