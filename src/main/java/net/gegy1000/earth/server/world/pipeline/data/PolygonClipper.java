package net.gegy1000.earth.server.world.pipeline.data;

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

    public PolygonClipper(double minX, double minY, double maxX, double maxY) {
        this.clipEdges = new Segment[] {
                new Segment(new Coordinate(minX, minY), new Coordinate(maxX, minY)),
                new Segment(new Coordinate(maxX, minY), new Coordinate(maxX, maxY)),
                new Segment(new Coordinate(maxX, maxY), new Coordinate(minX, maxY)),
                new Segment(new Coordinate(minX, maxY), new Coordinate(minX, minY))
        };
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

        Collection<Coordinate> uniqueCoordinates = Arrays.asList(ring.getCoordinates())
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
        return (clip.end.x - clip.start.x) * (p.y - clip.start.y) > (clip.end.y - clip.start.y) * (p.x - clip.start.x);
    }

    private Coordinate intersection(Segment clipEdge, Coordinate start, Coordinate end) {
        double deltaClipX = clipEdge.start.x - clipEdge.end.x;
        double deltaClipY = clipEdge.start.y - clipEdge.end.y;
        double deltaLineX = start.x - end.x;
        double deltaLineY = start.y - end.y;
        double clipDet = clipEdge.start.x * clipEdge.end.y - clipEdge.start.y * clipEdge.end.x;
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
        Coordinate start;
        Coordinate end;

        Segment(Coordinate start, Coordinate end) {
            this.start = start;
            this.end = end;
        }
    }
}
