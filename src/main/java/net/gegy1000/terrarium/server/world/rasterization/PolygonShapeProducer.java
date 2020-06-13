package net.gegy1000.terrarium.server.world.rasterization;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

public class PolygonShapeProducer {
    public static Area toShape(MultiPolygon polygon, Transform transform) {
        Area area = new Area();
        for (int i = 0; i < polygon.getNumGeometries(); i++) {
            Geometry geometry = polygon.getGeometryN(i);
            if (geometry instanceof Polygon) {
                area.add(toShape((Polygon) geometry, transform));
            }
        }
        return area;
    }

    public static Area toShape(Polygon polygon, Transform transform) {
        if (!polygon.isEmpty()) {
            Area exterior = getArea(polygon.getExteriorRing(), transform);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                Area interior = getArea(polygon.getInteriorRingN(i), transform);
                exterior.subtract(interior);
            }
            return exterior;
        }
        return new Area();
    }

    public static Area getArea(LineString ring, Transform transform) {
        Path2D path = toPath(ring, transform);
        path.closePath();

        return new Area(path);
    }

    public static Path2D toPath(LineString string, Transform transform) {
        Path2D.Double path = new Path2D.Double();

        Coordinate coordinate = string.getCoordinateN(0);
        path.moveTo(transform.x(coordinate.x), transform.y(coordinate.y));
        for (int i = 1; i < string.getNumPoints(); i++) {
            coordinate = string.getCoordinateN(i);
            path.lineTo(transform.x(coordinate.x), transform.y(coordinate.y));
        }

        return path;
    }

    public interface Transform {
        double x(double x);

        double y(double y);
    }
}
