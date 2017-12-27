package net.gegy1000.terrarium.server.util;

import com.google.common.collect.Sets;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class FloodFill {
    public static void floodVisit(int[] map, int width, int height, Point origin, IntVisitor visitor) {
        Queue<Point> points = new LinkedList<>();
        Set<Point> visitedPoints = Sets.newHashSet(origin);
        points.add(origin);

        while (!points.isEmpty()) {
            Point currentPoint = points.poll();
            int index = currentPoint.x + currentPoint.z * width;
            int value = map[index];
            int visited = visitor.visit(currentPoint, value);
            if (value != visited) {
                map[index] = visited;
            }

            if (currentPoint.x > 0) {
                int neighbourX = currentPoint.x - 1;
                int neighbourZ = currentPoint.z;
                int sampled = map[neighbourX + neighbourZ * width];
                FloodFill.visitNeighbour(points, visitedPoints, sampled, neighbourX, neighbourZ, visitor);
            }
            if (currentPoint.z > 0) {
                int neighbourX = currentPoint.x;
                int neighbourZ = currentPoint.z - 1;
                int sampled = map[neighbourX + neighbourZ * width];
                FloodFill.visitNeighbour(points, visitedPoints, sampled, neighbourX, neighbourZ, visitor);
            }

            if (currentPoint.x < width - 1) {
                int neighbourX = currentPoint.x + 1;
                int neighbourZ = currentPoint.z;
                int sampled = map[neighbourX + neighbourZ * width];
                FloodFill.visitNeighbour(points, visitedPoints, sampled, neighbourX, neighbourZ, visitor);
            }
            if (currentPoint.z < height - 1) {
                int neighbourX = currentPoint.x;
                int neighbourZ = currentPoint.z + 1;
                int sampled = map[neighbourX + neighbourZ * width];
                FloodFill.visitNeighbour(points, visitedPoints, sampled, neighbourX, neighbourZ, visitor);
            }
        }
    }

    public static <T> void floodVisit(T[] map, int width, int height, Point origin, Visitor<T> visitor) {
        Queue<Point> points = new LinkedList<>();
        Set<Point> visitedPoints = Sets.newHashSet(origin);
        points.add(origin);

        while (!points.isEmpty()) {
            Point currentPoint = points.poll();
            int index = currentPoint.x + currentPoint.z * width;
            T value = map[index];
            T visited = visitor.visit(currentPoint, value);
            if (value != visited) {
                if (visited == null) {
                    return;
                }
                map[index] = visited;
            }

            if (currentPoint.x > 0) {
                int neighbourX = currentPoint.x - 1;
                int neighbourZ = currentPoint.z;
                T sampled = map[neighbourX + neighbourZ * width];
                FloodFill.visitNeighbour(points, visitedPoints, sampled, neighbourX, neighbourZ, visitor);
            }
            if (currentPoint.z > 0) {
                int neighbourX = currentPoint.x;
                int neighbourZ = currentPoint.z - 1;
                T sampled = map[neighbourX + neighbourZ * width];
                FloodFill.visitNeighbour(points, visitedPoints, sampled, neighbourX, neighbourZ, visitor);
            }

            if (currentPoint.x < width - 1) {
                int neighbourX = currentPoint.x + 1;
                int neighbourZ = currentPoint.z;
                T sampled = map[neighbourX + neighbourZ * width];
                FloodFill.visitNeighbour(points, visitedPoints, sampled, neighbourX, neighbourZ, visitor);
            }
            if (currentPoint.z < height - 1) {
                int neighbourX = currentPoint.x;
                int neighbourZ = currentPoint.z + 1;
                T sampled = map[neighbourX + neighbourZ * width];
                FloodFill.visitNeighbour(points, visitedPoints, sampled, neighbourX, neighbourZ, visitor);
            }
        }
    }

    private static void visitNeighbour(Queue<Point> points, Set<Point> visitedPoints, int sampled, int neighbourX, int neighbourY, IntVisitor visitor) {
        Point neighbourPoint = new Point(neighbourX, neighbourY);
        if (visitor.canVisit(sampled) && !visitedPoints.contains(neighbourPoint)) {
            points.add(neighbourPoint);
            visitedPoints.add(neighbourPoint);
        }
    }

    private static <T> void visitNeighbour(Queue<Point> points, Set<Point> visitedPoints, T sampled, int neighbourX, int neighbourY, Visitor<T> visitor) {
        Point neighbourPoint = new Point(neighbourX, neighbourY);
        if (visitor.canVisit(sampled) && !visitedPoints.contains(neighbourPoint)) {
            points.add(neighbourPoint);
            visitedPoints.add(neighbourPoint);
        }
    }

    public interface Visitor<T> {
        T visit(Point point, T sampled);

        boolean canVisit(T sampled);
    }

    public interface IntVisitor {
        int visit(Point point, int sampled);

        boolean canVisit(int sampled);
    }

    public static class Point {
        private final int x;
        private final int z;

        public Point(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public int getX() {
            return this.x;
        }

        public int getZ() {
            return this.z;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point) {
                Point point = (Point) obj;
                return point.x == this.x && point.z == this.z;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.x + this.z * 12000;
        }
    }
}
