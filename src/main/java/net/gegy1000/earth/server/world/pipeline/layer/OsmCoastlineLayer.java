package net.gegy1000.earth.server.world.pipeline.layer;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataLayer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.adapter.debug.DebugImageWriter;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: If osm data is available, fill the area with land/water first
public class OsmCoastlineLayer extends OsmWaterLayer {
    protected static final int BANK_UP_FLAG = 0b100;
    protected static final int BANK_DOWN_FLAG = 0b1000;
    protected static final int FREE_FLOOD_FLAG = 0b100000;

    public static final DebugImageWriter.ColorSelector<Short> BANK_DEBUG = value -> {
        if ((value & FREE_FLOOD_FLAG) != 0) {
            return 0x404040;
        } else if ((value & BANK_UP_FLAG) != 0) {
            return 0xFF0000;
        } else if ((value & BANK_DOWN_FLAG) != 0) {
            return 0xFFFF00;
        }
        int type = value & TYPE_MASK;
        switch (type) {
            case OCEAN:
                return 0x0000FF;
            case RIVER:
                return 0x00AAFF;
            case LAND:
                return 0x00FF00;
            case BANK:
                return 0xFFFFFF;
        }
        return 0;
    };

    private final CoordinateState geoCoordinateState;

    public OsmCoastlineLayer(DataLayer<ShortRasterTile> parent, DataLayer<OsmTile> osmLayer, CoordinateState geoCoordinateState) {
        super(parent, osmLayer);
        this.geoCoordinateState = geoCoordinateState;
    }

    @Override
    protected ShortRasterTile applyWater(DataView view, ShortRasterTile waterTile, OsmTile osmTile) {
        List<OsmWay> coastlines = osmTile.getWays().stream()
                .filter(way -> OsmDataParser.hasTag(way, "natural", "coastline"))
                .collect(Collectors.toList());

        if (!coastlines.isEmpty()) {
            ShortRasterTile resultTile = waterTile.copy();
            short[] bankMap = resultTile.getShortData();

            for (OsmWay coastline : coastlines) {
                List<Point> linePoints = this.collectLinePoints(OsmDataParser.createLines(osmTile, coastline));
                this.rasterizeLine(view, resultTile, linePoints);
            }

            this.reduceProblematicPoints(view, bankMap);

            DebugImageWriter.write("coast_" + view.getX() + "_" + view.getY(), bankMap, BANK_DEBUG, view.getWidth(), view.getHeight());

            this.floodCoastMap(view, resultTile);
            DebugImageWriter.write("coast_" + view.getX() + "_" + view.getY() + "_b", bankMap, BANK_DEBUG, view.getWidth(), view.getHeight());

            return resultTile;
        }

        return waterTile;
    }

    protected List<Point> collectLinePoints(List<LineString> lines) {
        List<Point> linePoints = new LinkedList<>();
        for (LineString line : lines) {
            for (int i = 0; i < line.getNumPoints(); i++) {
                linePoints.add(line.getPointN(i));
            }
        }
        return linePoints;
    }

    protected void rasterizeLine(DataView view, ShortRasterTile resultTile, List<Point> line) {
        for (int nodeIndex = 1; nodeIndex < line.size(); nodeIndex++) {
            Point current = line.get(nodeIndex - 1);
            Point next = line.get(nodeIndex);

            Coordinate currentCoordinate = new Coordinate(this.geoCoordinateState, current.getX(), current.getY());

            double originX = currentCoordinate.getBlockX();
            double originY = currentCoordinate.getBlockZ();

            double minStep = 3.0;
            Coordinate nextCoordinate = new Coordinate(this.geoCoordinateState, next.getX(), next.getY());
            while (Math.abs(nextCoordinate.getBlockX() - originX) < minStep && Math.abs(nextCoordinate.getBlockZ() - originY) < minStep && ++nodeIndex < line.size()) {
                Point node = line.get(nodeIndex);
                nextCoordinate = new Coordinate(this.geoCoordinateState, node.getX(), node.getY());
            }

            double targetX = nextCoordinate.getBlockX();
            double targetY = nextCoordinate.getBlockZ();

            int bankType = this.selectBankType(currentCoordinate, nextCoordinate);
            this.rasterizeLineSegment(view, resultTile, originX, originY, targetX, targetY, bankType);
        }
    }

    protected int selectBankType(Coordinate currentCoordinate, Coordinate nextCoordinate) {
        int nextBlockZ = MathHelper.floor(nextCoordinate.getBlockZ());
        int currentBlockZ = MathHelper.floor(currentCoordinate.getBlockZ());
        if (nextBlockZ > currentBlockZ) {
            return BANK | BANK_DOWN_FLAG;
        } else if (nextBlockZ < currentBlockZ) {
            return BANK | BANK_UP_FLAG;
        }
        return BANK;
    }

    protected void rasterizeLineSegment(DataView view, ShortRasterTile resultTile, double originX, double originY, double targetX, double targetY, int bankType) {
        Interpolation.interpolateLine(originX, originY, targetX, targetY, false, point -> {
            int localX = point.x - view.getX();
            int localY = point.y - view.getY();
            if (localX >= 0 && localY >= 0 && localX < view.getWidth() && localY < view.getHeight()) {
                short currentBankType = resultTile.getShort(localX, localY);
                if ((currentBankType & TYPE_MASK) != BANK) {
                    resultTile.setShort(localX, localY, (short) bankType);
                } else {
                    resultTile.setShort(localX, localY, (short) (currentBankType | (bankType & ~TYPE_MASK)));
                }
                this.freeNeighbors(view, resultTile, localX, localY);
            }
        });
    }

    protected void freeNeighbors(DataView view, ShortRasterTile resultTile, int localX, int localY) {
        for (int neighbourY = -1; neighbourY <= 1; neighbourY++) {
            for (int neighbourX = -1; neighbourX <= 1; neighbourX++) {
                if (neighbourX != 0 || neighbourY != 0) {
                    int globalX = localX + neighbourX;
                    int globalY = localY + neighbourY;
                    if (globalX >= 0 && globalY >= 0 && globalX < view.getWidth() && globalY < view.getHeight()) {
                        int sample = resultTile.getShort(globalX, globalY);
                        if ((sample & TYPE_MASK) != BANK) {
                            resultTile.setShort(globalX, globalY, (short) (sample | FREE_FLOOD_FLAG));
                        }
                    }
                }
            }
        }
    }

    protected void reduceProblematicPoints(DataView view, short[] bankMap) {
        int width = view.getWidth();
        int height = view.getHeight();

        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                int bankType = bankMap[index];

                if (this.isFillingBankType(bankType)) {
                    if ((bankType & BANK_UP_FLAG) != 0 && (bankType & BANK_DOWN_FLAG) != 0) {
                        bankMap[index] = BANK;
                    } else {
                        int upType = (localY > 0) ? bankMap[index - width] : bankType;
                        int leftType = (localX > 0) ? bankMap[index - 1] : bankType;
                        int downType = (localY < height - 1) ? bankMap[index + width] : bankType;
                        int rightType = (localX < width - 1) ? bankMap[index + 1] : bankType;

                        if ((upType != bankType && downType != bankType) && (leftType != bankType && rightType != bankType)) {
                            bankMap[index] = BANK;
                        }
                    }
                }
            }
        }
    }

    protected boolean isFillingBankType(int bankType) {
        return (bankType & TYPE_MASK) == BANK && ((bankType & BANK_UP_FLAG) != 0 || (bankType & BANK_DOWN_FLAG) != 0);
    }

    protected void floodCoastMap(DataView view, ShortRasterTile resultTile) {
        Object2ShortMap<FloodFill.Point> floodSources = this.createFloodSources(view, resultTile);

        for (Map.Entry<FloodFill.Point, Short> entry : floodSources.entrySet()) {
            FloodFill.Point point = entry.getKey();
            int floodType = entry.getValue();
            short sampled = resultTile.getShort(point.getX(), point.getY());
            if ((sampled & TYPE_MASK) != BANK) {
                FillVisitor visitor = new FillVisitor((short) floodType);
                if (visitor.canVisit(point, sampled)) {
                    FloodFill.floodVisit(resultTile.getShortData(), view.getWidth(), view.getHeight(), point, visitor);
                }
            }
        }
    }

    protected Object2ShortMap<FloodFill.Point> createFloodSources(DataView view, ShortRasterTile resultTile) {
        Object2ShortMap<FloodFill.Point> floodSources = new Object2ShortOpenHashMap<>();

        for (int localY = 0; localY < view.getHeight(); localY++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                int sampled = resultTile.getShort(localX, localY);
                if (this.isFillingBankType(sampled)) {
                    if (localX > 0) {
                        short left = (sampled & BANK_UP_FLAG) != 0 ? LAND : OCEAN;
                        floodSources.put(new FloodFill.Point(localX - 1, localY), left);
                    }
                    if (localX < view.getWidth() - 1) {
                        short right = (sampled & BANK_UP_FLAG) != 0 ? OCEAN : LAND;
                        floodSources.put(new FloodFill.Point(localX + 1, localY), right);
                    }
                }
            }
        }

        return floodSources;
    }

    protected class FillVisitor implements FloodFill.ShortVisitor {
        protected final short floodType;

        protected FillVisitor(short floodType) {
            this.floodType = floodType;
        }

        @Override
        public short visit(FloodFill.Point point, short sampled) {
            if ((sampled & FREE_FLOOD_FLAG) != 0) {
                return (short) (this.floodType | FREE_FLOOD_FLAG);
            }
            return this.floodType;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, short sampled) {
            if ((sampled & FREE_FLOOD_FLAG) != 0) {
                return true;
            }
            int sampledType = sampled & TYPE_MASK;
            if (sampledType == BANK) {
                return false;
            }
            return sampledType != (this.floodType & TYPE_MASK);
        }
    }
}
