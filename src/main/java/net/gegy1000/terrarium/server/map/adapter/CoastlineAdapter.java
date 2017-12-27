package net.gegy1000.terrarium.server.map.adapter;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.source.osm.OverpassSource;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

public class CoastlineAdapter implements RegionAdapter {
    private static final int OCEAN = 0;
    private static final int LAND = 1;
    private static final int COAST = 2;

    private static final int COAST_UP = 4;
    private static final int COAST_DOWN = 8;
    private static final int FREE_FLOOD = 16;

    @Override
    public void adaptGlobcover(EarthGenerationSettings settings, OverpassTileAccess overpassTile, GlobType[] globBuffer, int x, int z, int width, int height) {
        List<OverpassSource.Element> coastlines = overpassTile.getElements().stream()
                .filter(element -> element.isType("natural", "coastline"))
                .collect(Collectors.toList());

        if (!coastlines.isEmpty()) {
            int[] landmap = new int[width * height];
            for (int i = 0; i < landmap.length; i++) {
                landmap[i] = globBuffer[i] == GlobType.WATER ? OCEAN : LAND;
            }

            Object2IntMap<FloodPoint> floodPoints = new Object2IntOpenHashMap<>();

            for (OverpassSource.Element coastline : coastlines) {
                List<OverpassSource.Element> nodes = coastline.collectNodes(overpassTile);

                for (int nodeIndex = 1; nodeIndex < nodes.size(); nodeIndex++) {
                    OverpassSource.Element current = nodes.get(nodeIndex - 1);
                    OverpassSource.Element next = nodes.get(nodeIndex);

                    Coordinate currentCoordinate = Coordinate.fromLatLng(settings, current.getLatitude(), current.getLongitude());

                    double originX = currentCoordinate.getBlockX();
                    double originZ = currentCoordinate.getBlockZ();

                    Coordinate nextCoordinate = Coordinate.fromLatLng(settings, next.getLatitude(), next.getLongitude());
                    while (Math.abs(nextCoordinate.getBlockX() - originX) < 3.0 && Math.abs(nextCoordinate.getBlockZ() - originZ) < 3.0) {
                        if (++nodeIndex >= nodes.size()) {
                            break;
                        }
                        OverpassSource.Element node = nodes.get(nodeIndex);
                        nextCoordinate = Coordinate.fromLatLng(settings, node.getLatitude(), node.getLongitude());
                    }

                    double targetX = nextCoordinate.getBlockX();
                    double targetZ = nextCoordinate.getBlockZ();

                    int lineType = this.getLineType(currentCoordinate, nextCoordinate);
                    int coastType = lineType & 252;

                    List<Point> points = new ArrayList<>();
                    Interpolation.interpolateLine(originX, originZ, targetX, targetZ, false, point -> {
                        int localX = point.x - x;
                        int localZ = point.y - z;
                        if (localX >= 0 && localZ >= 0 && localX < width && localZ < height) {
                            landmap[localX + localZ * width] = lineType;

                            for (int neighbourZ = -1; neighbourZ <= 1; neighbourZ++) {
                                for (int neighbourX = -1; neighbourX <= 1; neighbourX++) {
                                    if (neighbourX != 0 || neighbourZ != 0) {
                                        int globalX = localX + neighbourX;
                                        int globalZ = localZ + neighbourZ;
                                        if (globalX >= 0 && globalZ >= 0 && globalX < width && globalZ < height) {
                                            int index = globalX + globalZ * width;
                                            int sample = landmap[index];
                                            if ((sample & 3) != COAST) {
                                                landmap[index] = sample | FREE_FLOOD;
                                            }
                                        }
                                    }
                                }
                            }

                            points.add(point);
                        }
                    });

                    if (points.size() > 2) {
                        for (int i = 1; i < points.size() - 1; i++) {
                            Point point = points.get(i);
                            int localX = point.x - x;
                            int localZ = point.y - z;
                            if (coastType != 0) {
                                if (localX > 0) {
                                    int left = coastType == COAST_UP ? LAND : OCEAN;
                                    floodPoints.put(new FloodPoint(localX - 1, localZ), left);
                                }
                                if (localX < width - 1) {
                                    int right = coastType == COAST_UP ? OCEAN : LAND;
                                    floodPoints.put(new FloodPoint(localX + 1, localZ), right);
                                }
                            }
                        }
                    }
                }
            }

            for (Map.Entry<FloodPoint, Integer> entry : floodPoints.entrySet()) {
                FloodPoint point = entry.getKey();
                int floodType = entry.getValue();
                int sampled = landmap[point.x + point.z * width];
                if (this.canFlood(sampled, floodType)) {
                    this.floodFill(landmap, width, height, point, floodType);
                }
            }

            for (int i = 0; i < globBuffer.length; i++) {
                GlobType glob = globBuffer[i];
                int landType = landmap[i] & 3;
                if (landType == OCEAN) {
                    globBuffer[i] = GlobType.WATER;
                } else if (glob == GlobType.WATER) {
                    // TODO: Select proper glob type based on neighbours
                    globBuffer[i] = GlobType.GRASSLAND;
                }
            }
        }
    }

    private void floodFill(int[] landmap, int width, int height, FloodPoint origin, int floodType) {
        Stack<FloodPoint> points = new Stack<>();
        Set<FloodPoint> visitedPoints = Sets.newHashSet(origin);
        points.push(origin);

        while (!points.isEmpty()) {
            FloodPoint currentPoint = points.pop();
            int index = currentPoint.x + currentPoint.z * width;
            if ((landmap[index] & 252) == FREE_FLOOD) {
                landmap[index] = floodType | FREE_FLOOD;
            } else {
                landmap[index] = floodType;
            }

            if (currentPoint.x > 0) {
                int neighbourX = currentPoint.x - 1;
                int neighbourZ = currentPoint.z;
                int sampled = landmap[neighbourX + neighbourZ * width];
                this.visitNeighbour(points, visitedPoints, sampled, floodType, neighbourX, neighbourZ);
            }
            if (currentPoint.z > 0) {
                int neighbourX = currentPoint.x;
                int neighbourZ = currentPoint.z - 1;
                int sampled = landmap[neighbourX + neighbourZ * width];
                this.visitNeighbour(points, visitedPoints, sampled, floodType, neighbourX, neighbourZ);
            }

            if (currentPoint.x < width - 1) {
                int neighbourX = currentPoint.x + 1;
                int neighbourZ = currentPoint.z;
                int sampled = landmap[neighbourX + neighbourZ * width];
                this.visitNeighbour(points, visitedPoints, sampled, floodType, neighbourX, neighbourZ);
            }
            if (currentPoint.z < height - 1) {
                int neighbourX = currentPoint.x;
                int neighbourZ = currentPoint.z + 1;
                int sampled = landmap[neighbourX + neighbourZ * width];
                this.visitNeighbour(points, visitedPoints, sampled, floodType, neighbourX, neighbourZ);
            }
        }
    }

    private void visitNeighbour(Stack<FloodPoint> points, Set<FloodPoint> visitedPoints, int sampled, int floodType, int neighbourX, int neighbourY) {
        FloodPoint neighbourPoint = new FloodPoint(neighbourX, neighbourY);
        if (this.canFlood(sampled, floodType) && !visitedPoints.contains(neighbourPoint)) {
            points.push(neighbourPoint);
            visitedPoints.add(neighbourPoint);
        }
    }

    private boolean canFlood(int sampled, int flood) {
        int landType = sampled & 3;
        return (landType == LAND || landType == OCEAN) && (landType != (flood & 3) || (sampled & 252) == FREE_FLOOD);
    }

    private int getLineType(Coordinate currentCoordinate, Coordinate nextCoordinate) {
        int nextBlockZ = MathHelper.floor(nextCoordinate.getBlockZ());
        int currentBlockZ = MathHelper.floor(currentCoordinate.getBlockZ());
        if (nextBlockZ > currentBlockZ) {
            return COAST | COAST_DOWN;
        } else if (nextBlockZ < currentBlockZ) {
            return COAST | COAST_UP;
        }
        return COAST;
    }

    private class FloodPoint {
        private final int x;
        private final int z;

        private FloodPoint(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FloodPoint) {
                FloodPoint point = (FloodPoint) obj;
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
