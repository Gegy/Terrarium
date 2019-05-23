package net.gegy1000.terrarium.server.world.rasterization;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;

import java.awt.geom.Area;
import java.awt.geom.Path2D;

public class PolygonShapeProducer {
    public static Area toShape(MultiPolygon polygon, CoordinateState state) {
        Area area = new Area();
        for (int i = 0; i < polygon.getNumGeometries(); i++) {
            Geometry geometry = polygon.getGeometryN(i);
            if (geometry instanceof Polygon) {
                area.add(toShape((Polygon) geometry, state));
            }
        }
        return area;
    }

    public static Area toShape(Polygon polygon, CoordinateState state) {
        if (!polygon.isEmpty()) {
            Area exterior = getArea(polygon.getExteriorRing(), state);
            for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                Area interior = getArea(polygon.getInteriorRingN(i), state);
                exterior.subtract(interior);
            }
            return exterior;
        }
        return new Area();
    }

    public static Area getArea(LineString ring, CoordinateState state) {
        Path2D.Double path = new Path2D.Double();

        Coordinate coordinate = ring.getCoordinateN(0);
        path.moveTo(state.getBlockX(coordinate.x, coordinate.y), state.getBlockZ(coordinate.x, coordinate.y));
        for (int i = 1; i < ring.getNumPoints(); i++) {
            coordinate = ring.getCoordinateN(i);
            path.lineTo(state.getBlockX(coordinate.x, coordinate.y), state.getBlockZ(coordinate.x, coordinate.y));
        }
        path.closePath();

        return new Area(path);
    }

    public static Path2D toPath(LineString string, CoordinateState state) {
        Path2D.Double path = new Path2D.Double();

        Coordinate coordinate = string.getCoordinateN(0);
        path.moveTo(state.getBlockX(coordinate.x, coordinate.y), state.getBlockZ(coordinate.x, coordinate.y));
        for (int i = 1; i < string.getNumPoints(); i++) {
            coordinate = string.getCoordinateN(i);
            path.lineTo(state.getBlockX(coordinate.x, coordinate.y), state.getBlockZ(coordinate.x, coordinate.y));
        }

        return path;
    }
}
