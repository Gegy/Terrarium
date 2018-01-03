package net.gegy1000.terrarium.server.util;

import com.google.common.collect.Sets;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class FloodFill {
    public static void floodVisit(int[] map, int width, int height, Point origin, IntVisitor visitor) {
        Set<Point> visitedPoints = Sets.newHashSet(origin);
        Queue<Point> points = new LinkedList<>();
        points.add(origin);

        while (!points.isEmpty()) {
            Point currentPoint = points.poll();
            int index = currentPoint.x + currentPoint.y * width;
            int value = map[index];
            int visited = visitor.visit(currentPoint, value);
            if (value != visited) {
                map[index] = visited;
            }

            for (Offset offset : Offset.VALUES) {
                Point neighbourPoint = offset.offset(currentPoint);
                if (neighbourPoint.x >= 0 && neighbourPoint.y >= 0 && neighbourPoint.x < width && neighbourPoint.y < height) {
                    int sampled = map[neighbourPoint.x + neighbourPoint.y * width];
                    if (visitor.canVisit(neighbourPoint, sampled)) {
                        FloodFill.visitNeighbour(points, visitedPoints, neighbourPoint);
                    }
                }
            }
        }
    }

    public static void floodVisit(short[] map, int width, int height, Point origin, ShortVisitor visitor) {
        Set<Point> visitedPoints = Sets.newHashSet(origin);
        Queue<Point> points = new LinkedList<>();
        points.add(origin);

        while (!points.isEmpty()) {
            Point currentPoint = points.poll();
            int index = currentPoint.x + currentPoint.y * width;
            short value = map[index];
            short visited = visitor.visit(currentPoint, value);
            if (value != visited) {
                map[index] = visited;
            }

            for (Offset offset : Offset.VALUES) {
                Point neighbourPoint = offset.offset(currentPoint);
                if (neighbourPoint.x >= 0 && neighbourPoint.y >= 0 && neighbourPoint.x < width && neighbourPoint.y < height) {
                    short sampled = map[neighbourPoint.x + neighbourPoint.y * width];
                    if (visitor.canVisit(neighbourPoint, sampled)) {
                        FloodFill.visitNeighbour(points, visitedPoints, neighbourPoint);
                    }
                }
            }
        }
    }

    public static <T> void floodVisit(T[] map, int width, int height, Point origin, Visitor<T> visitor) {
        Set<Point> visitedPoints = Sets.newHashSet(origin);
        Queue<Point> points = new LinkedList<>();
        points.add(origin);

        while (!points.isEmpty()) {
            Point currentPoint = points.poll();
            int index = currentPoint.x + currentPoint.y * width;
            T value = map[index];
            T visited = visitor.visit(currentPoint, value);
            if (value != visited) {
                if (visited == null) {
                    return;
                }
                map[index] = visited;
            }

            for (Offset offset : Offset.VALUES) {
                Point neighbourPoint = offset.offset(currentPoint);
                if (neighbourPoint.x >= 0 && neighbourPoint.y >= 0 && neighbourPoint.x < width && neighbourPoint.y < height) {
                    T sampled = map[neighbourPoint.x + neighbourPoint.y * width];
                    if (visitor.canVisit(neighbourPoint, sampled)) {
                        FloodFill.visitNeighbour(points, visitedPoints, neighbourPoint);
                    }
                }
            }
        }
    }

    private static void visitNeighbour(Queue<Point> points, Set<Point> visitedPoints, Point neighbourPoint) {
        if (!visitedPoints.contains(neighbourPoint)) {
            points.add(neighbourPoint);
            visitedPoints.add(neighbourPoint);
        }
    }

    public interface Visitor<T> {
        T visit(Point point, T sampled);

        boolean canVisit(Point point, T sampled);
    }

    public interface IntVisitor {
        int visit(Point point, int sampled);

        boolean canVisit(Point point, int sampled);
    }

    public interface ShortVisitor {
        short visit(Point point, short sampled);

        boolean canVisit(Point point, short sampled);
    }

    public static class Point {
        private final int x;
        private final int y;
        private final int hash;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
            this.hash = x + y << 16;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point) {
                Point point = (Point) obj;
                return point.x == this.x && point.y == this.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.hash;
        }
    }

    private enum Offset {
        UP(0, 1),
        DOWN(0, -1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        private static final Offset[] VALUES = values();

        private final int offsetX;
        private final int offsetY;

        Offset(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public Point offset(Point point) {
            return new Point(point.x + this.offsetX, point.y + this.offsetY);
        }
    }
}
