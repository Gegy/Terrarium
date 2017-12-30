package net.gegy1000.terrarium.server.map.adapter;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.source.osm.OverpassSource;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoastlineAdapter implements RegionAdapter {
    private static final int OCEAN = 0;
    private static final int LAND = 1;
    private static final int COAST = 2;

    private static final int COAST_UP = 4;
    private static final int COAST_DOWN = 8;
    private static final int FREE_FLOOD = 16;

    @Override
    public void adapt(EarthGenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        OverpassTileAccess overpassTile = data.getOverpassTile();

        short[] heightBuffer = data.getHeights();
        GlobType[] globBuffer = data.getGlobcover();

        List<OverpassSource.Element> coastlines = overpassTile.getElements().stream()
                .filter(element -> element.isType("natural", "coastline"))
                .collect(Collectors.toList());

        if (!coastlines.isEmpty()) {
            int[] landmap = new int[width * height];
            for (int i = 0; i < landmap.length; i++) {
                landmap[i] = globBuffer[i] == GlobType.WATER ? OCEAN : LAND;
            }

            Object2IntMap<FloodFill.Point> floodPoints = new Object2IntOpenHashMap<>();

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

                            this.freeNeighbours(width, height, landmap, localX, localZ);

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
                                    floodPoints.put(new FloodFill.Point(localX - 1, localZ), left);
                                }
                                if (localX < width - 1) {
                                    int right = coastType == COAST_UP ? OCEAN : LAND;
                                    floodPoints.put(new FloodFill.Point(localX + 1, localZ), right);
                                }
                            }
                        }
                    }
                }
            }

            for (Map.Entry<FloodFill.Point, Integer> entry : floodPoints.entrySet()) {
                FloodFill.Point point = entry.getKey();
                int floodType = entry.getValue();
                int sampled = landmap[point.getX() + point.getZ() * width];
                FillVisitor visitor = new FillVisitor(floodType);
                if (visitor.canVisit(point, sampled)) {
                    FloodFill.floodVisit(landmap, width, height, point, visitor);
                }
            }

            List<FloodFill.Point> unselectedPoints = new LinkedList<>();
            for (int i = 0; i < globBuffer.length; i++) {
                GlobType glob = globBuffer[i];
                int landType = landmap[i] & 3;
                if (landType == OCEAN) {
                    if (globBuffer[i] != GlobType.WATER) {
                        globBuffer[i] = GlobType.WATER;
                        heightBuffer[i] = 1;
                    }
                } else if (glob == GlobType.WATER) {
                    globBuffer[i] = GlobType.PROCESSING;
                    unselectedPoints.add(new FloodFill.Point(i % width, i / width));
                }
            }

            for (FloodFill.Point point : unselectedPoints) {
                GlobSelectVisitor visitor = new GlobSelectVisitor();
                FloodFill.floodVisit(globBuffer, width, height, point, visitor);
                globBuffer[point.getX() + point.getZ() * width] = visitor.getResult();
            }
        }
    }

    private void freeNeighbours(int width, int height, int[] landmap, int localX, int localZ) {
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

    private class FillVisitor implements FloodFill.IntVisitor {
        private final int floodType;

        private FillVisitor(int floodType) {
            this.floodType = floodType;
        }

        @Override
        public int visit(FloodFill.Point point, int sampled) {
            if ((sampled & 252) == FREE_FLOOD) {
                return this.floodType | FREE_FLOOD;
            }
            return this.floodType;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, int sampled) {
            int landType = sampled & 3;
            return (landType == LAND || landType == OCEAN) && (landType != (this.floodType & 3) || (sampled & 252) == FREE_FLOOD);
        }
    }

    private class GlobSelectVisitor implements FloodFill.Visitor<GlobType> {
        private GlobType result = GlobType.RAINFED_CROPS;

        @Override
        public GlobType visit(FloodFill.Point point, GlobType sampled) {
            if (sampled != GlobType.PROCESSING) {
                this.result = sampled;
                return null;
            }
            return sampled;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, GlobType sampled) {
            return sampled != GlobType.WATER;
        }

        public GlobType getResult() {
            return this.result;
        }
    }
}
