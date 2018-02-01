package net.gegy1000.terrarium.server.map.adapter;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.gegy1000.terrarium.server.map.RegionData;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.osm.OsmDataParser;
import net.gegy1000.terrarium.server.map.source.glob.CoverTileAccess;
import net.gegy1000.terrarium.server.map.source.height.HeightTileAccess;
import net.gegy1000.terrarium.server.map.source.osm.OverpassTileAccess;
import net.gegy1000.terrarium.server.map.system.component.TerrariumComponentTypes;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CoastlineAdapter implements RegionAdapter {
    public static final int OCEAN = 0;
    public static final int LAND = 1;
    public static final int COAST = 2;

    public static final int COAST_UP = 4;
    public static final int COAST_DOWN = 8;
    public static final int FREE_FLOOD = 16;
    public static final int COAST_IGNORE = 32;

    public static final int LAND_TYPE_MASK = 3;
    public static final int COAST_TYPE_MASK = 252;

    @Override
    public void adapt(EarthGenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        HeightTileAccess heightTile = data.get(TerrariumComponentTypes.HEIGHT);
        CoverTileAccess coverTile = data.get(TerrariumComponentTypes.COVER);
        OverpassTileAccess overpassTile = data.get(TerrariumComponentTypes.OVERPASS);

        if (heightTile == null || coverTile == null || overpassTile == null) {
            return;
        }

        short[] heightBuffer = heightTile.getShortData();
        CoverType[] coverBuffer = coverTile.getData();

        List<OsmWay> coastlines = overpassTile.getWays().valueCollection().stream()
                .filter(way -> OsmDataParser.hasTag(way, "natural", "coastline"))
                .collect(Collectors.toList());

        if (!coastlines.isEmpty()) {
            int[] landmap = new int[width * height];
            for (int i = 0; i < landmap.length; i++) {
                landmap[i] = coverBuffer[i] == CoverType.WATER ? OCEAN : LAND;
            }

            for (OsmWay coastline : coastlines) {
                List<Point> linePoints = new LinkedList<>();
                List<LineString> lines = OsmDataParser.createLines(overpassTile, coastline);

                for (LineString line : lines) {
                    for (int i = 0; i < line.getNumPoints(); i++) {
                        linePoints.add(line.getPointN(i));
                    }
                }

                this.rasterizeLine(settings, x, z, width, height, landmap, linePoints);
            }

            this.removeProblematicLines(width, height, landmap);

            this.floodFillMap(width, height, landmap);

            this.processFloodedMap(width, height, heightBuffer, coverBuffer, landmap);
        }
    }

    private void rasterizeLine(EarthGenerationSettings settings, int x, int y, int width, int height, int[] landmap, List<Point> line) {
        for (int nodeIndex = 1; nodeIndex < line.size(); nodeIndex++) {
            Point current = line.get(nodeIndex - 1);
            Point next = line.get(nodeIndex);

            Coordinate currentCoordinate = Coordinate.fromLatLng(settings, current.getX(), current.getY());

            double originX = currentCoordinate.getBlockX();
            double originY = currentCoordinate.getBlockZ();

            Coordinate nextCoordinate = Coordinate.fromLatLng(settings, next.getX(), next.getY());
            while (Math.abs(nextCoordinate.getBlockX() - originX) < 3.0 && Math.abs(nextCoordinate.getBlockZ() - originY) < 3.0 && ++nodeIndex < line.size()) {
                Point node = line.get(nodeIndex);
                nextCoordinate = Coordinate.fromLatLng(settings, node.getX(), node.getY());
            }

            double targetX = nextCoordinate.getBlockX();
            double targetY = nextCoordinate.getBlockZ();

            int lineType = this.getLineType(currentCoordinate, nextCoordinate);
            int coastType = lineType & COAST_TYPE_MASK;

            this.rasterizeLineSegment(x, y, width, height, landmap, originX, originY, targetX, targetY, lineType, coastType);
        }
    }

    private void rasterizeLineSegment(int x, int y, int width, int height, int[] landmap, double originX, double originY, double targetX, double targetY, int lineType, int coastType) {
        List<FloodFill.Point> points = new ArrayList<>();
        Interpolation.interpolateLine(originX, originY, targetX, targetY, false, point -> {
            int localX = point.x - x;
            int localY = point.y - y;
            if (localX >= 0 && localY >= 0 && localX < width && localY < height) {
                landmap[localX + localY * width] = COAST | COAST_IGNORE;

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
                    landmap[localX + localY * width] = lineType;
                }
            }
        }
    }

    private void freeNeighbours(int width, int height, int[] landmap, int localX, int localY) {
        this.iterateNeighbours(localX, localY, width, height, true, neighbourIndex -> {
            int sample = landmap[neighbourIndex];
            if ((sample & LAND_TYPE_MASK) != COAST) {
                landmap[neighbourIndex] = sample | FREE_FLOOD;
            }
        });
    }

    private void removeProblematicLines(int width, int height, int[] landmap) {
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                int coastType = landmap[index] & COAST_TYPE_MASK;

                if (this.isFillingCoastType(coastType)) {
                    this.iterateNeighbours(localX, localY, width, height, false, neighbourIndex -> {
                        int neighbour = landmap[neighbourIndex];
                        int neighbourCoastType = neighbour & COAST_TYPE_MASK;

                        if (this.isFillingCoastType(neighbourCoastType) && coastType != neighbourCoastType) {
                            landmap[neighbourIndex] = COAST | COAST_IGNORE;
                            landmap[index] = COAST | COAST_IGNORE;
                        }
                    });
                }
            }
        }
    }

    private void iterateNeighbours(int originX, int originY, int width, int height, boolean corners, Consumer<Integer> neighbourIndex) {
        for (int neighbourY = -1; neighbourY <= 1; neighbourY++) {
            for (int neighbourX = -1; neighbourX <= 1; neighbourX++) {
                if (neighbourX != 0 || neighbourY != 0) {
                    if (corners || this.isNeighbourCorner(neighbourX, neighbourY)) {
                        int globalX = originX + neighbourX;
                        int globalY = originY + neighbourY;
                        if (globalX >= 0 && globalY >= 0 && globalX < width && globalY < height) {
                            int index = globalX + globalY * width;
                            neighbourIndex.accept(index);
                        }
                    }
                }
            }
        }
    }

    private boolean isNeighbourCorner(int neighbourX, int neighbourY) {
        return neighbourX != 0 && neighbourY != 0;
    }

    private void floodFillMap(int width, int height, int[] landmap) {
        Object2IntMap<FloodFill.Point> floodSources = this.createFloodSources(width, height, landmap);

        for (Map.Entry<FloodFill.Point, Integer> entry : floodSources.entrySet()) {
            FloodFill.Point point = entry.getKey();
            int floodType = entry.getValue();
            int sampled = landmap[point.getX() + point.getY() * width];
            FillVisitor visitor = new FillVisitor(floodType);
            if (visitor.canVisit(point, sampled)) {
                FloodFill.floodVisit(landmap, width, height, point, visitor);
            }
        }
    }

    private Object2IntMap<FloodFill.Point> createFloodSources(int width, int height, int[] landmap) {
        Object2IntMap<FloodFill.Point> floodSources = new Object2IntOpenHashMap<>();

        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int coastType = landmap[localX + localY * width] & COAST_TYPE_MASK;
                if (this.isFillingCoastType(coastType)) {
                    if (localX > 0) {
                        int left = coastType == COAST_UP ? LAND : OCEAN;
                        floodSources.put(new FloodFill.Point(localX - 1, localY), left);
                    }
                    if (localX < width - 1) {
                        int right = coastType == COAST_UP ? OCEAN : LAND;
                        floodSources.put(new FloodFill.Point(localX + 1, localY), right);
                    }
                }
            }
        }

        return floodSources;
    }

    private void processFloodedMap(int width, int height, short[] heightBuffer, CoverType[] coverBuffer, int[] landmap) {
        List<FloodFill.Point> unselectedPoints = new LinkedList<>();
        for (int i = 0; i < coverBuffer.length; i++) {
            CoverType glob = coverBuffer[i];
            int sample = landmap[i];
            int landType = sample & LAND_TYPE_MASK;
            int coastType = sample & COAST_TYPE_MASK;
            if (landType == OCEAN) {
                if (coverBuffer[i] != CoverType.WATER) {
                    coverBuffer[i] = CoverType.WATER;
                    heightBuffer[i] = 1;
                }
            } else if ((landType == LAND || landType == COAST && coastType != FREE_FLOOD) && glob == CoverType.WATER) {
                coverBuffer[i] = CoverType.PROCESSING;
                unselectedPoints.add(new FloodFill.Point(i % width, i / width));
            }
        }

        for (FloodFill.Point point : unselectedPoints) {
            GlobSelectVisitor visitor = new GlobSelectVisitor();
            FloodFill.floodVisit(coverBuffer, width, height, point, visitor);
            coverBuffer[point.getX() + point.getY() * width] = visitor.getResult();
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

    private boolean isFillingCoastType(int lineType) {
        int coastType = lineType & COAST_TYPE_MASK;
        return coastType == COAST_UP || coastType == COAST_DOWN;
    }

    private class FillVisitor implements FloodFill.IntVisitor {
        private final int floodType;

        private FillVisitor(int floodType) {
            this.floodType = floodType;
        }

        @Override
        public int visit(FloodFill.Point point, int sampled) {
            if ((sampled & COAST_TYPE_MASK) == FREE_FLOOD) {
                return this.floodType | FREE_FLOOD;
            }
            return this.floodType;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, int sampled) {
            int landType = sampled & LAND_TYPE_MASK;
            return (landType == LAND || landType == OCEAN) && (landType != (this.floodType & 3) || (sampled & COAST_TYPE_MASK) == FREE_FLOOD);
        }
    }

    private class GlobSelectVisitor implements FloodFill.Visitor<CoverType> {
        private CoverType result = null;

        @Override
        public CoverType visit(FloodFill.Point point, CoverType sampled) {
            if (sampled != CoverType.PROCESSING) {
                this.result = sampled;
                return null;
            }
            return sampled;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, CoverType sampled) {
            return sampled != CoverType.WATER;
        }

        public CoverType getResult() {
            if (this.result == null) {
                return CoverType.RAINFED_CROPS;
            }
            return this.result;
        }
    }
}
