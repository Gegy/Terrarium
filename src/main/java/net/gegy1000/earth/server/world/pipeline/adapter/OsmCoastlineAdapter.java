package net.gegy1000.earth.server.world.pipeline.adapter;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.adapter.debug.DebugImageWriter;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OsmCoastlineAdapter implements RegionAdapter {
    private static final DebugImageWriter.ColorSelector<Integer> COASTLINE_DEBUG = value -> {
        int coastType = value & OsmCoastlineAdapter.COAST_TYPE_MASK;
        switch (coastType) {
            case OsmCoastlineAdapter.FREE_FLOOD:
                return 0x404040;
            case OsmCoastlineAdapter.COAST_UP:
                return 0xFF0000;
            case OsmCoastlineAdapter.COAST_DOWN:
                return 0xFFFF00;
            case OsmCoastlineAdapter.COAST_IGNORE:
                return 0xFFFFFF;
        }
        int landType = value & OsmCoastlineAdapter.LAND_TYPE_MASK;
        switch (landType) {
            case OsmCoastlineAdapter.OCEAN:
                return 0x0000FF;
            case OsmCoastlineAdapter.LAND:
                return 0x00FF00;
            case OsmCoastlineAdapter.COAST:
                return 0x009000;
        }
        return 0;
    };

    public static final int OCEAN = 0;
    public static final int LAND = 1;
    public static final int COAST = 2;

    public static final int COAST_UP = 4;
    public static final int COAST_DOWN = 8;
    public static final int FREE_FLOOD = 16;
    public static final int COAST_IGNORE = 32;

    public static final int LAND_TYPE_MASK = 3;
    public static final int COAST_TYPE_MASK = 252;

    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<CoverRasterTile> coverComponent;
    private final RegionComponentType<OsmTile> osmComponent;
    private final CoordinateState latLngCoordinateState;

    public OsmCoastlineAdapter(RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<CoverRasterTile> coverComponent, RegionComponentType<OsmTile> osmComponent, CoordinateState latLngCoordinateState) {
        this.heightComponent = heightComponent;
        this.coverComponent = coverComponent;
        this.osmComponent = osmComponent;
        this.latLngCoordinateState = latLngCoordinateState;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);
        CoverRasterTile coverTile = data.getOrExcept(this.coverComponent);
        OsmTile osmTile = data.getOrExcept(this.osmComponent);

        short[] heightBuffer = heightTile.getShortData();
        CoverType[] coverBuffer = coverTile.getData();

        List<OsmWay> coastlines = osmTile.getWays().valueCollection().stream()
                .filter(way -> OsmDataParser.hasTag(way, "natural", "coastline"))
                .collect(Collectors.toList());

        if (!coastlines.isEmpty()) {
            int[] landmap = new int[width * height];
            for (int i = 0; i < landmap.length; i++) {
                landmap[i] = coverBuffer[i] == EarthCoverTypes.WATER ? OCEAN : LAND;
            }

            for (OsmWay coastline : coastlines) {
                List<Point> linePoints = new LinkedList<>();
                List<LineString> lines = OsmDataParser.createLines(osmTile, coastline);

                for (LineString line : lines) {
                    for (int i = 0; i < line.getNumPoints(); i++) {
                        linePoints.add(line.getPointN(i));
                    }
                }

                this.rasterizeLine(x, z, width, height, landmap, linePoints);
            }

            this.removeProblematicLines(width, height, landmap);

            this.floodFillMap(width, height, landmap);

            this.processFloodedMap(width, height, heightBuffer, coverBuffer, landmap);
        }
    }

    private void rasterizeLine(int x, int y, int width, int height, int[] landmap, List<Point> line) {
        for (int nodeIndex = 1; nodeIndex < line.size(); nodeIndex++) {
            Point current = line.get(nodeIndex - 1);
            Point next = line.get(nodeIndex);

            Coordinate currentCoordinate = new Coordinate(this.latLngCoordinateState, current.getX(), current.getY());

            double originX = currentCoordinate.getBlockX();
            double originY = currentCoordinate.getBlockZ();

            Coordinate nextCoordinate = new Coordinate(this.latLngCoordinateState, next.getX(), next.getY());
            while (Math.abs(nextCoordinate.getBlockX() - originX) < 3.0 && Math.abs(nextCoordinate.getBlockZ() - originY) < 3.0 && ++nodeIndex < line.size()) {
                Point node = line.get(nodeIndex);
                nextCoordinate = new Coordinate(this.latLngCoordinateState, node.getX(), node.getY());
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
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                CoverType cover = coverBuffer[index];
                int sample = landmap[index];
                int landType = sample & LAND_TYPE_MASK;
                int coastType = sample & COAST_TYPE_MASK;
                if (landType == OCEAN) {
                    if (cover != EarthCoverTypes.WATER) {
                        coverBuffer[index] = EarthCoverTypes.WATER;
                        heightBuffer[index] = 0;
                    }
                } else if ((landType == LAND || landType == COAST && coastType != FREE_FLOOD) && cover == EarthCoverTypes.WATER) {
                    coverBuffer[index] = TerrariumCoverTypes.PLACEHOLDER;
                    unselectedPoints.add(new FloodFill.Point(localX, localY));
                }
            }
        }

        for (FloodFill.Point point : unselectedPoints) {
            CoverSelectVisitor visitor = new CoverSelectVisitor();
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

    private class CoverSelectVisitor implements FloodFill.Visitor<CoverType> {
        private CoverType result = null;

        @Override
        public CoverType visit(FloodFill.Point point, CoverType sampled) {
            if (sampled != TerrariumCoverTypes.PLACEHOLDER) {
                this.result = sampled;
                return null;
            }
            return sampled;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, CoverType sampled) {
            return sampled != EarthCoverTypes.WATER;
        }

        public CoverType getResult() {
            if (this.result == null) {
                return EarthCoverTypes.RAINFED_CROPS;
            }
            return this.result;
        }
    }
}
