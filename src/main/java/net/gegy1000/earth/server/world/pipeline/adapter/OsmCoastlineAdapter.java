package net.gegy1000.earth.server.world.pipeline.adapter;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.gegy1000.earth.server.world.cover.EarthCoverTypes;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.earth.server.world.pipeline.source.tile.WaterRasterTile;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.TerrariumCoverTypes;
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
    private static final int TYPE_MASK = 0b11;
    private static final int FLAGS_MASK = ~TYPE_MASK;

    private static final int WATER = 0b0;
    private static final int LAND = 0b1;
    private static final int COAST = 0b10;

    private static final int COAST_UP_FLAG = 0b100;
    private static final int COAST_DOWN_FLAG = 0b1000;
    private static final int FREE_FLOOD_FLAG = 0b100000;

    private static final DebugImageWriter.ColorSelector<Integer> COASTLINE_DEBUG = value -> {
        if ((value & FREE_FLOOD_FLAG) != 0) {
            return 0x404040;
        } else if ((value & COAST_UP_FLAG) != 0) {
            return 0xFF0000;
        } else if ((value & COAST_DOWN_FLAG) != 0) {
            return 0xFFFF00;
        }
        int type = value & TYPE_MASK;
        switch (type) {
            case WATER:
                return 0x0000FF;
            case LAND:
                return 0x00FF00;
            case COAST:
                return 0xFFFFFF;
        }
        return 0;
    };

    private final CoordinateState latLngCoordinateState;
    private final RegionComponentType<OsmTile> osmComponent;
    private final RegionComponentType<WaterRasterTile> waterComponent;
    private final RegionComponentType<ShortRasterTile> heightComponent;
    private final RegionComponentType<CoverRasterTile> coverComponent;

    public OsmCoastlineAdapter(CoordinateState latLngCoordinateState, RegionComponentType<OsmTile> osmComponent, RegionComponentType<WaterRasterTile> waterComponent, RegionComponentType<ShortRasterTile> heightComponent, RegionComponentType<CoverRasterTile> coverComponent) {
        this.latLngCoordinateState = latLngCoordinateState;
        this.osmComponent = osmComponent;
        this.waterComponent = waterComponent;
        this.heightComponent = heightComponent;
        this.coverComponent = coverComponent;
    }

    @Override
    public void adapt(GenerationSettings settings, RegionData data, int x, int z, int width, int height) {
        OsmTile osmTile = data.getOrExcept(this.osmComponent);

        List<OsmWay> coastlines = osmTile.getWays().valueCollection().stream()
                .filter(way -> OsmDataParser.hasTag(way, "natural", "coastline"))
                .collect(Collectors.toList());

        if (!coastlines.isEmpty()) {
            WaterRasterTile waterTile = data.getOrExcept(this.waterComponent);
            ShortRasterTile heightTile = data.getOrExcept(this.heightComponent);
            CoverRasterTile coverTile = data.getOrExcept(this.coverComponent);
            short[] waterBuffer = waterTile.getShortData();

            int[] coastMap = new int[width * height];

            for (int i = 0; i < coastMap.length; i++) {
                coastMap[i] = waterBuffer[i] == WaterRasterTile.LAND ? LAND : WATER;
            }

            for (OsmWay coastline : coastlines) {
                List<Point> linePoints = this.collectLinePoints(OsmDataParser.createLines(osmTile, coastline));
                this.rasterizeLine(x, z, width, height, coastMap, linePoints);
            }

            this.removeProblematicPoints(width, height, coastMap);

            this.floodCoastMap(width, height, coastMap);
            this.applyFloodedMap(width, height, waterTile, heightTile.getShortData(), coverTile.getData(), coastMap);
        }
    }

    private List<Point> collectLinePoints(List<LineString> lines) {
        List<Point> linePoints = new LinkedList<>();
        for (LineString line : lines) {
            for (int i = 0; i < line.getNumPoints(); i++) {
                linePoints.add(line.getPointN(i));
            }
        }
        return linePoints;
    }

    private void rasterizeLine(int x, int y, int width, int height, int[] coastMap, List<Point> line) {
        for (int nodeIndex = 1; nodeIndex < line.size(); nodeIndex++) {
            Point current = line.get(nodeIndex - 1);
            Point next = line.get(nodeIndex);

            Coordinate currentCoordinate = new Coordinate(this.latLngCoordinateState, current.getX(), current.getY());

            double originX = currentCoordinate.getBlockX();
            double originY = currentCoordinate.getBlockZ();

            double minStep = 3.0;
            Coordinate nextCoordinate = new Coordinate(this.latLngCoordinateState, next.getX(), next.getY());
            while (Math.abs(nextCoordinate.getBlockX() - originX) < minStep && Math.abs(nextCoordinate.getBlockZ() - originY) < minStep && ++nodeIndex < line.size()) {
                Point node = line.get(nodeIndex);
                nextCoordinate = new Coordinate(this.latLngCoordinateState, node.getX(), node.getY());
            }

            double targetX = nextCoordinate.getBlockX();
            double targetY = nextCoordinate.getBlockZ();

            int coastType = this.selectCoastType(currentCoordinate, nextCoordinate);
            this.rasterizeLineSegment(x, y, width, height, coastMap, originX, originY, targetX, targetY, coastType);
        }
    }

    private int selectCoastType(Coordinate currentCoordinate, Coordinate nextCoordinate) {
        int nextBlockZ = MathHelper.floor(nextCoordinate.getBlockZ());
        int currentBlockZ = MathHelper.floor(currentCoordinate.getBlockZ());
        if (nextBlockZ > currentBlockZ) {
            return COAST | COAST_DOWN_FLAG;
        } else if (nextBlockZ < currentBlockZ) {
            return COAST | COAST_UP_FLAG;
        }
        return COAST;
    }

    private void rasterizeLineSegment(int x, int y, int width, int height, int[] coastMap, double originX, double originY, double targetX, double targetY, int coastType) {
        List<FloodFill.Point> points = new ArrayList<>();
        Interpolation.interpolateLine(originX, originY, targetX, targetY, false, point -> {
            int localX = point.x - x;
            int localY = point.y - y;
            if (localX >= 0 && localY >= 0 && localX < width && localY < height) {
                this.freeNeighbors(width, height, coastMap, localX, localY);
                points.add(new FloodFill.Point(localX, localY));
            }
        });

        if (this.isFillingCoastType(coastType) && points.size() > 2) {
            for (int i = 0; i < points.size(); i++) {
                FloodFill.Point point = points.get(i);
                int index = point.getX() + point.getY() * width;
                if (i < 1 && i >= points.size() - 1 || (coastMap[index] & TYPE_MASK) == COAST) {
                    coastMap[index] = COAST;
                } else {
                    coastMap[index] = coastType;
                }
            }
        } else {
            for (FloodFill.Point point : points) {
                int index = point.getX() + point.getY() * width;
                coastMap[index] = COAST;
            }
        }
    }

    private void freeNeighbors(int width, int height, int[] coastMap, int localX, int localY) {
        this.iterateNeighbours(localX, localY, width, height, true, neighborIndex -> {
            int sample = coastMap[neighborIndex];
            if ((sample & TYPE_MASK) != COAST) {
                coastMap[neighborIndex] = sample | FREE_FLOOD_FLAG;
            }
        });
    }

    private void removeProblematicPoints(int width, int height, int[] coastMap) {
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                int coastType = coastMap[index];

                if (this.isFillingCoastType(coastType)) {
                    this.iterateNeighbours(localX, localY, width, height, false, neighbourIndex -> {
                        int neighbourType = coastMap[neighbourIndex];

                        if (this.isFillingCoastType(neighbourType) && this.areDifferentDirection(coastType, neighbourType)) {
                            coastMap[neighbourIndex] = COAST;
                            coastMap[index] = COAST;
                        }
                    });
                }
            }
        }
    }

    private boolean isFillingCoastType(int coastType) {
        return (coastType & TYPE_MASK) == COAST && ((coastType & COAST_UP_FLAG) != 0 || (coastType & COAST_DOWN_FLAG) != 0);
    }

    private boolean areDifferentDirection(int leftCoastType, int rightCoastType) {
        boolean differentUp = ((leftCoastType & COAST_UP_FLAG) != 0) ^ ((rightCoastType & COAST_UP_FLAG) != 0);
        boolean differentDown = ((leftCoastType & COAST_DOWN_FLAG) != 0) ^ ((rightCoastType & COAST_DOWN_FLAG) != 0);
        return differentUp && differentDown;
    }

    private void iterateNeighbours(int originX, int originY, int width, int height, boolean corners, Consumer<Integer> neighbourIndex) {
        for (int neighbourY = -1; neighbourY <= 1; neighbourY++) {
            for (int neighbourX = -1; neighbourX <= 1; neighbourX++) {
                if (neighbourX != 0 || neighbourY != 0) {
                    if (corners || !this.isNeighbourCorner(neighbourX, neighbourY)) {
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

    private void floodCoastMap(int width, int height, int[] coastMap) {
        Object2IntMap<FloodFill.Point> floodSources = this.createFloodSources(width, height, coastMap);

        for (Map.Entry<FloodFill.Point, Integer> entry : floodSources.entrySet()) {
            FloodFill.Point point = entry.getKey();
            int floodType = entry.getValue();
            int sampled = coastMap[point.getX() + point.getY() * width];
            if ((sampled & TYPE_MASK) != COAST) {
                FillVisitor visitor = new FillVisitor(floodType);
                if (visitor.canVisit(point, sampled)) {
                    FloodFill.floodVisit(coastMap, width, height, point, visitor);
                }
            }
        }
    }

    private Object2IntMap<FloodFill.Point> createFloodSources(int width, int height, int[] coastMap) {
        Object2IntMap<FloodFill.Point> floodSources = new Object2IntOpenHashMap<>();

        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int sampled = coastMap[localX + localY * width];
                if (this.isFillingCoastType(sampled)) {
                    if (localX > 0) {
                        int left = (sampled & COAST_UP_FLAG) != 0 ? LAND : WATER;
                        floodSources.put(new FloodFill.Point(localX - 1, localY), left);
                    }
                    if (localX < width - 1) {
                        int right = (sampled & COAST_UP_FLAG) != 0 ? WATER : LAND;
                        floodSources.put(new FloodFill.Point(localX + 1, localY), right);
                    }
                }
            }
        }

        return floodSources;
    }

    private void applyFloodedMap(int width, int height, WaterRasterTile waterTile, short[] heightBuffer, CoverType[] coverBuffer, int[] coastMap) {
        List<FloodFill.Point> unselectedPoints = new LinkedList<>();
        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                int sample = coastMap[index];
                int waterType = waterTile.getWaterType(localX, localY);
                int sampledType = sample & TYPE_MASK;
                if (sampledType == WATER) {
                    if (waterType == WaterRasterTile.LAND) {
                        coverBuffer[index] = EarthCoverTypes.WATER;
                        waterTile.setWaterType(localX, localY, WaterRasterTile.OCEAN);
                    }
                } else {
                    if (waterType != WaterRasterTile.LAND) {
                        coverBuffer[index] = TerrariumCoverTypes.PLACEHOLDER;
                        unselectedPoints.add(new FloodFill.Point(localX, localY));
                        waterTile.setWaterType(localX, localY, WaterRasterTile.LAND);
                        heightBuffer[index] = (short) Math.max(1, heightBuffer[index]);
                    }
                }
            }
        }

        for (FloodFill.Point point : unselectedPoints) {
            CoverSelectVisitor visitor = new CoverSelectVisitor();
            FloodFill.floodVisit(coverBuffer, width, height, point, visitor);
            coverBuffer[point.getX() + point.getY() * width] = visitor.getResult();
        }
    }

    private class FillVisitor implements FloodFill.IntVisitor {
        private final int floodType;

        private FillVisitor(int floodType) {
            this.floodType = floodType;
        }

        @Override
        public int visit(FloodFill.Point point, int sampled) {
            if ((sampled & FREE_FLOOD_FLAG) != 0) {
                return this.floodType | FREE_FLOOD_FLAG;
            }
            return this.floodType;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, int sampled) {
            if ((sampled & FREE_FLOOD_FLAG) != 0) {
                return true;
            }
            int sampledType = sampled & TYPE_MASK;
            if (sampledType == COAST) {
                return false;
            }
            return sampledType != (this.floodType & TYPE_MASK);
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
