package net.gegy1000.earth.server.world.data.op;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import javax.annotation.Nullable;
import java.util.ArrayList;
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
        Collection<Polygon> polygons = null;

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon polygon = this.clip((Polygon) multiPolygon.getGeometryN(i));
            if (polygon != null) {
                if (polygons == null) {
                    polygons = new ArrayList<>(multiPolygon.getNumGeometries());
                }
                polygons.add(polygon);
            }
        }

        if (polygons == null) return null;

        return GEOMETRY_FACTORY.createMultiPolygon(polygons.toArray(new Polygon[0]));
    }

    @Nullable
    public Polygon clip(Polygon polygon) {
        LinearRing exteriorRing = this.clip((LinearRing) polygon.getExteriorRing());
        if (exteriorRing == null) return null;

        Collection<LinearRing> interiorRings = null;
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            LinearRing clippedRing = this.clip((LinearRing) polygon.getInteriorRingN(i));
            if (clippedRing != null) {
                if (interiorRings == null) {
                    interiorRings = new ArrayList<>(polygon.getNumInteriorRing());
                }
                interiorRings.add(clippedRing);
            }
        }

        LinearRing[] interiorRingsArray = interiorRings != null ? interiorRings.toArray(new LinearRing[0]) : null;
        return GEOMETRY_FACTORY.createPolygon(exteriorRing, interiorRingsArray);
    }

    @Nullable
    public LinearRing clip(LinearRing ring) {
        if (ring.getNumPoints() <= 0) {
            return null;
        }

        Coordinate[] coordinates = ring.getCoordinates();

        // double-buffered setup to minimise allocation
        List<Coordinate> clipped = new ArrayList<>(coordinates.length - 1);
        for (int i = 0; i < coordinates.length - 1; i++) {
            clipped.add(coordinates[i]);
        }

        List<Coordinate> output = new ArrayList<>(clipped.size() / 4);

        for (Segment clipEdge : this.clipEdges) {
            output.clear();
            this.clipEdge(clipped, clipEdge, output);

            // we don't have enough points to form a line
            if (output.size() < 2) {
                return null;
            }

            // swap the clipped and output buffers
            List<Coordinate> swap = clipped;
            clipped = output;
            output = swap;
        }

        Coordinate[] clippedArray = new Coordinate[clipped.size() + 1];
        for (int i = 0; i < clipped.size(); i++) {
            clippedArray[i] = clipped.get(i);
        }

        // close the line
        clippedArray[clippedArray.length - 1] = clipped.get(0);

        return GEOMETRY_FACTORY.createLinearRing(clippedArray);
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

    private void clipEdge(List<Coordinate> input, Segment clipEdge, List<Coordinate> output) {
        Coordinate startCoord = input.get(input.size() - 1);
        for (Coordinate endCoord : input) {
            boolean startInside = this.isInside(startCoord, clipEdge);
            boolean endInside = this.isInside(endCoord, clipEdge);

            if (endInside) {
                if (!startInside) {
                    output.add(this.intersection(clipEdge, startCoord, endCoord));
                }
                output.add(endCoord);
            } else if (startInside) {
                output.add(this.intersection(clipEdge, startCoord, endCoord));
            }

            startCoord = endCoord;
        }
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
