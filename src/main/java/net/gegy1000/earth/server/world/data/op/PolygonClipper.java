package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class PolygonClipper {
    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    private final Segment[] clipEdges;

    private PolygonClipper(Segment[] clipEdges) {
        this.clipEdges = clipEdges;
    }

    public static PolygonClipper rect(double minX, double minY, double maxX, double maxY) {
        return new PolygonClipper(new Segment[] {
                new Segment(minX, minY, maxX, minY),
                new Segment(maxX, minY, maxX, maxY),
                new Segment(maxX, maxY, minX, maxY),
                new Segment(minX, maxY, minX, minY)
        });
    }

    @Nullable
    public MultiPolygon clip(MultiPolygon multiPolygon) {
        Collection<Polygon> polygons = new ArrayList<>(multiPolygon.getNumGeometries());

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon polygon = this.clip((Polygon) multiPolygon.getGeometryN(i));
            if (polygon != null) {
                polygons.add(polygon);
            }
        }

        if (polygons.isEmpty()) {
            return null;
        }

        return GEOMETRY_FACTORY.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }

    @Nullable
    public Polygon clip(Polygon polygon) {
        LinearRing exteriorRing = this.clip((LinearRing) polygon.getExteriorRing());
        if (exteriorRing == null) {
            return null;
        }

        Collection<LinearRing> interiorRings = new ArrayList<>(polygon.getNumInteriorRing());
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            LinearRing clippedRing = this.clip((LinearRing) polygon.getInteriorRingN(i));
            if (clippedRing != null) {
                interiorRings.add(clippedRing);
            }
        }

        return GEOMETRY_FACTORY.createPolygon(exteriorRing, interiorRings.toArray(new LinearRing[0]));
    }

    @Nullable
    public LinearRing clip(LinearRing ring) {
        if (ring.getNumPoints() <= 0) {
            return null;
        }

        Coordinate[] coordinates = ring.getCoordinates();
        Collection<Coordinate> uniqueCoordinates = Arrays.asList(coordinates)
                .subList(0, coordinates.length - 1);

        List<Coordinate> result = new ArrayList<>(uniqueCoordinates);

        for (Segment clipEdge : this.clipEdges) {
            result = this.clipEdge(result, clipEdge);
            if (result.isEmpty()) {
                return null;
            }
        }

        Coordinate closePoint = result.get(0);
        result.add(closePoint);

        return GEOMETRY_FACTORY.createLinearRing(result.toArray(new Coordinate[0]));
    }

    private boolean isInside(Coordinate p, Segment clip) {
        return (clip.endX - clip.startX) * (p.y - clip.startY) > (clip.endY - clip.startY) * (p.x - clip.startX);
    }

    private Coordinate intersection(Segment clipEdge, Coordinate start, Coordinate end) {
        double deltaClipX = clipEdge.startX - clipEdge.endX;
        double deltaClipY = clipEdge.startY - clipEdge.endY;
        double deltaLineX = start.x - end.x;
        double deltaLineY = start.y - end.y;
        double clipDet = clipEdge.startX * clipEdge.endY - clipEdge.startY * clipEdge.endX;
        double lineDet = start.x * end.y - start.y * end.x;
        double det = deltaClipX * deltaLineY - deltaClipY * deltaLineX;
        return new Coordinate(
                (clipDet * deltaLineX - lineDet * deltaClipX) / det,
                (clipDet * deltaLineY - lineDet * deltaClipY) / det
        );
    }

    private List<Coordinate> clipEdge(List<Coordinate> coordinates, Segment clipEdge) {
        List<Coordinate> result = new ArrayList<>(coordinates.size());

        Coordinate startCoord = coordinates.get(coordinates.size() - 1);
        for (Coordinate endCoord : coordinates) {
            boolean startInside = this.isInside(startCoord, clipEdge);
            boolean endInside = this.isInside(endCoord, clipEdge);

            if (endInside) {
                if (!startInside) {
                    result.add(this.intersection(clipEdge, startCoord, endCoord));
                }
                result.add(endCoord);
            } else if (startInside) {
                result.add(this.intersection(clipEdge, startCoord, endCoord));
            }

            startCoord = endCoord;
        }

        return result;
    }

    private static class Segment {
        double startX;
        double startY;
        double endX;
        double endY;

        Segment(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }
    }
}
