package net.gegy1000.terrarium.server.map.adapter;

import com.vividsolutions.jts.geom.LineString;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.map.osm.OsmDataParser;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import com.vividsolutions.jts.geom.Point;
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

        List<OsmWay> coastlines = overpassTile.getWays().valueCollection().stream()
                .filter(way -> OsmDataParser.hasTag(way, "natural", "coastline"))
                .collect(Collectors.toList());

        if (!coastlines.isEmpty()) {
            int[] landmap = new int[width * height];
            for (int i = 0; i < landmap.length; i++) {
                landmap[i] = globBuffer[i] == GlobType.WATER ? OCEAN : LAND;
            }

            Object2IntMap<FloodFill.Point> floodPoints = new Object2IntOpenHashMap<>();

            for (OsmWay coastline : coastlines) {
                List<LineString> lines = OsmDataParser.createLines(overpassTile, coastline);

                for (LineString line : lines) {
                    this.drawLine(settings, x, z, width, height, landmap, floodPoints, line);
                }
            }

            for (Map.Entry<FloodFill.Point, Integer> entry : floodPoints.entrySet()) {
                FloodFill.Point point = entry.getKey();
                int floodType = entry.getValue();
                int sampled = landmap[point.getX() + point.getY() * width];
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
                } else if (landType == LAND && glob == GlobType.WATER) {
                    globBuffer[i] = GlobType.PROCESSING;
                    unselectedPoints.add(new FloodFill.Point(i % width, i / width));
                }
            }

            for (FloodFill.Point point : unselectedPoints) {
                GlobSelectVisitor visitor = new GlobSelectVisitor();
                FloodFill.floodVisit(globBuffer, width, height, point, visitor);
                globBuffer[point.getX() + point.getY() * width] = visitor.getResult();
            }
        }
    }

    private void drawLine(EarthGenerationSettings settings, int x, int y, int width, int height, int[] landmap, Object2IntMap<FloodFill.Point> floodPoints, LineString line) {
        for (int nodeIndex = 1; nodeIndex < line.getNumPoints(); nodeIndex++) {
            Point current = line.getPointN(nodeIndex - 1);
            Point next = line.getPointN(nodeIndex);

            Coordinate currentCoordinate = Coordinate.fromLatLng(settings, current.getX(), current.getY());

            double originX = currentCoordinate.getBlockX();
            double originY = currentCoordinate.getBlockZ();

            Coordinate nextCoordinate = Coordinate.fromLatLng(settings, next.getX(), next.getY());
            while (Math.abs(nextCoordinate.getBlockX() - originX) < 3.0 && Math.abs(nextCoordinate.getBlockZ() - originY) < 3.0) {
                if (++nodeIndex >= line.getNumPoints()) {
                    break;
                }
                Point node = line.getPointN(nodeIndex);
                nextCoordinate = Coordinate.fromLatLng(settings, node.getX(), node.getY());
            }

            double targetX = nextCoordinate.getBlockX();
            double targetY = nextCoordinate.getBlockZ();

            int lineType = this.getLineType(currentCoordinate, nextCoordinate);
            int coastType = lineType & 252;

            List<FloodFill.Point> points = new ArrayList<>();
            Interpolation.interpolateLine(originX, originY, targetX, targetY, false, point -> {
                int localX = point.x - x;
                int localY = point.y - y;
                if (localX >= 0 && localY >= 0 && localX < width && localY < height) {
                    landmap[localX + localY * width] = lineType;

                    this.freeNeighbours(width, height, landmap, localX, localY);

                    points.add(new FloodFill.Point(point.x, point.y));
                }
            });

            if (points.size() > 2) {
                for (int i = 1; i < points.size() - 1; i++) {
                    FloodFill.Point point = points.get(i);
                    int localX = point.getX() - x;
                    int localY = point.getY() - y;
                    if (coastType != 0) {
                        if (localX > 0) {
                            int left = coastType == COAST_UP ? LAND : OCEAN;
                            floodPoints.put(new FloodFill.Point(localX - 1, localY), left);
                        }
                        if (localX < width - 1) {
                            int right = coastType == COAST_UP ? OCEAN : LAND;
                            floodPoints.put(new FloodFill.Point(localX + 1, localY), right);
                        }
                    }
                }
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
        private GlobType result = null;

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
            if (this.result == null) {
                return GlobType.RAINFED_CROPS;
            }
            return this.result;
        }
    }
}
