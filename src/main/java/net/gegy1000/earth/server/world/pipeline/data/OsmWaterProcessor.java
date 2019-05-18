package net.gegy1000.earth.server.world.pipeline.data;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmData;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.util.Interpolation;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.DataOp;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.minecraft.util.math.MathHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.gegy1000.earth.server.world.pipeline.data.WaterProducer.*;

// TODO: This is problematic
public final class OsmWaterProcessor {
    public static DataOp<ShortRaster> mergeOsmCoastlines(DataOp<ShortRaster> water, DataOp<OsmData> osm, CoordinateState geoCoordinates) {
        return DataOp.of((engine, view) -> {
            CompletableFuture<ShortRaster> waterFuture = engine.load(water, view);
            CompletableFuture<OsmData> osmFuture = engine.load(osm, view);

            return CompletableFuture.allOf(waterFuture, osmFuture)
                    .thenApply(v -> {
                        ShortRaster waterRaster = waterFuture.join();
                        OsmData osmData = osmFuture.join();

                        List<OsmWay> coastlines = osmData.getWays().stream()
                                .filter(way -> OsmDataParser.hasTag(way, "natural", "coastline"))
                                .collect(Collectors.toList());

                        if (!coastlines.isEmpty()) {
                            ShortRaster result = waterRaster.copy();
                            short[] bankMap = result.getData();

                            for (OsmWay coastline : coastlines) {
                                List<Point> linePoints = collectLinePoints(OsmDataParser.createLines(osmData, coastline));
                                rasterizeLine(geoCoordinates, view, result, linePoints);
                            }

                            reduceProblematicPoints(view, bankMap);

                            floodCoastMap(view, result);

                            return result;
                        }

                        return waterRaster;
                    });
        });
    }

    private static List<Point> collectLinePoints(List<LineString> lines) {
        List<Point> linePoints = new LinkedList<>();
        for (LineString line : lines) {
            for (int i = 0; i < line.getNumPoints(); i++) {
                linePoints.add(line.getPointN(i));
            }
        }
        return linePoints;
    }

    private static void rasterizeLine(CoordinateState geoCoordinates, DataView view, ShortRaster resultTile, List<Point> line) {
        for (int nodeIndex = 1; nodeIndex < line.size(); nodeIndex++) {
            Point current = line.get(nodeIndex - 1);
            Point next = line.get(nodeIndex);

            Coordinate currentCoordinate = new Coordinate(geoCoordinates, current.getX(), current.getY());

            double originX = currentCoordinate.getBlockX();
            double originY = currentCoordinate.getBlockZ();

            double minStep = 3.0;
            Coordinate nextCoordinate = new Coordinate(geoCoordinates, next.getX(), next.getY());
            while (Math.abs(nextCoordinate.getBlockX() - originX) < minStep && Math.abs(nextCoordinate.getBlockZ() - originY) < minStep && ++nodeIndex < line.size()) {
                Point node = line.get(nodeIndex);
                nextCoordinate = new Coordinate(geoCoordinates, node.getX(), node.getY());
            }

            double targetX = nextCoordinate.getBlockX();
            double targetY = nextCoordinate.getBlockZ();

            int bankType = selectBankType(currentCoordinate, nextCoordinate);
            rasterizeLineSegment(view, resultTile, originX, originY, targetX, targetY, bankType);
        }
    }

    private static int selectBankType(Coordinate currentCoordinate, Coordinate nextCoordinate) {
        int nextBlockZ = MathHelper.floor(nextCoordinate.getBlockZ());
        int currentBlockZ = MathHelper.floor(currentCoordinate.getBlockZ());
        if (nextBlockZ > currentBlockZ) {
            return BANK | BANK_DOWN_FLAG;
        } else if (nextBlockZ < currentBlockZ) {
            return BANK | BANK_UP_FLAG;
        }
        return BANK;
    }

    private static void rasterizeLineSegment(DataView view, ShortRaster resultTile, double originX, double originY, double targetX, double targetY, int bankType) {
        Interpolation.interpolateLine(originX, originY, targetX, targetY, false, point -> {
            int localX = point.x - view.getX();
            int localY = point.y - view.getY();
            if (localX >= 0 && localY >= 0 && localX < view.getWidth() && localY < view.getHeight()) {
                short currentBankType = resultTile.get(localX, localY);
                if ((currentBankType & TYPE_MASK) != BANK) {
                    resultTile.set(localX, localY, (short) bankType);
                } else {
                    resultTile.set(localX, localY, (short) (currentBankType | (bankType & ~TYPE_MASK)));
                }
                freeNeighbors(view, resultTile, localX, localY);
            }
        });
    }

    private static void freeNeighbors(DataView view, ShortRaster resultTile, int localX, int localY) {
        for (int neighbourY = -1; neighbourY <= 1; neighbourY++) {
            for (int neighbourX = -1; neighbourX <= 1; neighbourX++) {
                if (neighbourX != 0 || neighbourY != 0) {
                    int globalX = localX + neighbourX;
                    int globalY = localY + neighbourY;
                    if (globalX >= 0 && globalY >= 0 && globalX < view.getWidth() && globalY < view.getHeight()) {
                        int sample = resultTile.get(globalX, globalY);
                        if ((sample & TYPE_MASK) != BANK) {
                            resultTile.set(globalX, globalY, (short) (sample | FREE_FLOOD_FLAG));
                        }
                    }
                }
            }
        }
    }

    private static void reduceProblematicPoints(DataView view, short[] bankMap) {
        int width = view.getWidth();
        int height = view.getHeight();

        for (int localY = 0; localY < height; localY++) {
            for (int localX = 0; localX < width; localX++) {
                int index = localX + localY * width;
                int bankType = bankMap[index];

                if (isFillingBankType(bankType)) {
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

    private static boolean isFillingBankType(int bankType) {
        return (bankType & TYPE_MASK) == BANK && ((bankType & BANK_UP_FLAG) != 0 || (bankType & BANK_DOWN_FLAG) != 0);
    }

    private static void floodCoastMap(DataView view, ShortRaster resultTile) {
        Object2ShortMap<FloodFill.Point> floodSources = createFloodSources(view, resultTile);

        for (Map.Entry<FloodFill.Point, Short> entry : floodSources.entrySet()) {
            FloodFill.Point point = entry.getKey();
            int floodType = entry.getValue();
            short sampled = resultTile.get(point.getX(), point.getY());
            if ((sampled & TYPE_MASK) != BANK) {
                FillVisitor visitor = new FillVisitor((short) floodType);
                if (visitor.canVisit(point, sampled)) {
                    FloodFill.floodVisit(resultTile.getData(), view.getWidth(), view.getHeight(), point, visitor);
                }
            }
        }
    }

    private static Object2ShortMap<FloodFill.Point> createFloodSources(DataView view, ShortRaster resultTile) {
        Object2ShortMap<FloodFill.Point> floodSources = new Object2ShortOpenHashMap<>();

        for (int localY = 0; localY < view.getHeight(); localY++) {
            for (int localX = 0; localX < view.getWidth(); localX++) {
                int sampled = resultTile.get(localX, localY);
                if (isFillingBankType(sampled)) {
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

    protected static class FillVisitor implements FloodFill.ShortVisitor {
        final short floodType;

        FillVisitor(short floodType) {
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
