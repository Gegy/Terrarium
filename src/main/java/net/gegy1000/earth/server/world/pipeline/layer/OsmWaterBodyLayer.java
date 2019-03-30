/*package net.gegy1000.earth.server.world.pipeline.layer;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmData;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.data.DataView;
import net.gegy1000.terrarium.server.world.pipeline.adapter.debug.DebugImageWriter;
import net.gegy1000.terrarium.server.world.pipeline.data.raster.ShortRaster;
import net.gegy1000.terrarium.server.world.rasterization.OsmShapeProducer;
import net.gegy1000.terrarium.server.world.rasterization.RasterCanvas;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Collection;

public class OsmWaterBodyLayer extends OsmWaterLayer {
    private static final int RIVER_COLOR = 1;
    private static final int CENTER_COLOR = 2;

    private final CoordinateState geoCoordinateState;

    public OsmWaterBodyLayer(DataLayer<ShortRaster> parent, DataLayer<OsmData> osmLayer, CoordinateState geoCoordinateState) {
        super(parent, osmLayer);
        this.geoCoordinateState = geoCoordinateState;
    }

    @Override
    protected ShortRaster applyWater(DataView view, ShortRaster waterTile, OsmData osmTile) {
        Collection<MultiPolygon> waterPolygons = osmTile.collectPolygons(view, this.geoCoordinateState, this::isWaterArea);
        Collection<LineString> waterLines = osmTile.collectLines(view, this.geoCoordinateState, this::isWaterLine);

        if (!waterPolygons.isEmpty() || !waterLines.isEmpty()) {
            ShortRaster resultTile = waterTile.copy();

            RasterCanvas canvas = new RasterCanvas(view.getWidth(), view.getHeight());
            canvas.setOrigin(view.getX(), view.getY());

            canvas.setColor(RIVER_COLOR);
            for (MultiPolygon polygon : waterPolygons) {
                Area shape = OsmShapeProducer.toShape(polygon, this.geoCoordinateState);
                Rectangle bounds = shape.getBounds();
                if (bounds.getWidth() > 2 && bounds.getHeight() > 2) {
                    canvas.fill(shape);
                }
            }

            canvas.setStroke(new BasicStroke(2));
            canvas.setColor(CENTER_COLOR);
            for (LineString line : waterLines) {
                Path2D path = OsmShapeProducer.toPath(line, this.geoCoordinateState);
                canvas.draw(path);
            }
            canvas.resetStroke();

            this.fillExistingWater(view, resultTile, canvas);
            DebugImageWriter.write("water_" + view.getX() + "_" + view.getY(), resultTile.getData(), BANK_DEBUG, view.getWidth(), view.getHeight());

            for (int localZ = 0; localZ < view.getHeight(); localZ++) {
                for (int localX = 0; localX < view.getWidth(); localX++) {
                    int value = canvas.getData(localX, localZ);
                    if (value == RIVER_COLOR) {
                        resultTile.setShort(localX, localZ, RIVER);
                    } else if (value == CENTER_COLOR) {
                        resultTile.setShort(localX, localZ, (short) (RIVER | CENTER_FLAG));
                    }
                }
            }

            DebugImageWriter.write("water_" + view.getX() + "_" + view.getY() + "_b", resultTile.getData(), BANK_DEBUG, view.getWidth(), view.getHeight());

            return resultTile;
        }

        return waterTile;
    }

    private boolean isWaterArea(OsmEntity entity) {
        boolean waterArea = OsmDataParser.hasTag(entity, "waterway", "riverbank")
                || OsmDataParser.hasTag(entity, "natural", "water")
                || OsmDataParser.hasTag(entity, "water", "river");
        return waterArea && !OsmDataParser.hasKey(entity, "tunnel");
    }

    private boolean isWaterLine(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "waterway", "river") && !OsmDataParser.hasKey(entity, "tunnel");
    }

    private void fillExistingWater(DataView view, ShortRaster resultTile, RasterCanvas canvas) {
        int width = view.getWidth();
        int height = view.getHeight();
        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                this.fillExistingWater(view, resultTile, canvas, localX, localZ);
            }
        }
    }

    private void fillExistingWater(DataView view, ShortRaster resultTile, RasterCanvas canvas, int localX, int localZ) {
        int value = canvas.getData(localX, localZ);
        if (value == RIVER_COLOR) {
            FloodFill.Point origin = new FloodFill.Point(localX, localZ);
            PreparationFloodVisitor visitor = new PreparationFloodVisitor();
            if (visitor.canVisit(origin, resultTile.getShort(localX, localZ))) {
                FloodFill.floodVisit(resultTile.getShortData(), view.getWidth(), view.getHeight(), origin, visitor);
            }
        }
    }

    private static class PreparationFloodVisitor implements FloodFill.ShortVisitor {
        @Override
        public short visit(FloodFill.Point point, short sampled) {
            return LAND;
        }

        @Override
        public boolean canVisit(FloodFill.Point point, short sampled) {
            int sampledType = sampled & TYPE_MASK;
            return sampledType == RIVER;
        }
    }
}*/
