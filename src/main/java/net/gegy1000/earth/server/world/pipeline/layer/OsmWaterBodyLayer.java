package net.gegy1000.earth.server.world.pipeline.layer;

import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.server.util.FloodFill;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataLayerProducer;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.ShortRasterTile;
import net.gegy1000.terrarium.server.world.rasterization.OsmShapeProducer;
import net.gegy1000.terrarium.server.world.rasterization.RasterCanvas;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OsmWaterBodyLayer extends OsmWaterLayer {
    private static final int RIVER_COLOR = 1;

    private final CoordinateState geoCoordinateState;

    public OsmWaterBodyLayer(DataLayerProducer<OsmTile> osmLayer, CoordinateState geoCoordinateState) {
        super(osmLayer);
        this.geoCoordinateState = geoCoordinateState;
    }

    @Override
    protected ShortRasterTile applyWater(DataView view, ShortRasterTile waterTile, OsmTile osmTile) {
        // TODO: Line-based rivers
        List<MultiPolygon> waterPolygons = this.collectWaterPolygons(osmTile);

        if (!waterPolygons.isEmpty()) {
            ShortRasterTile resultTile = waterTile.copy();

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

            this.fillExistingWater(view, resultTile, canvas);

            for (int localZ = 0; localZ < view.getHeight(); localZ++) {
                for (int localX = 0; localX < view.getWidth(); localX++) {
                    int value = canvas.getData(localX, localZ);
                    if (value == RIVER_COLOR) {
                        resultTile.setShort(localX, localZ, RIVER);
                    }
                }
            }

            return resultTile;
        }

        return waterTile;
    }

    private List<MultiPolygon> collectWaterPolygons(OsmTile osmTile) {
        List<MultiPolygon> waterPolygons = osmTile.getRelations().stream()
                .filter(this::isWaterArea)
                .map(relation -> OsmDataParser.createArea(osmTile, relation))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        waterPolygons.addAll(osmTile.getWays().stream()
                .filter(this::isWaterArea)
                .map(way -> OsmDataParser.createArea(osmTile, way))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return waterPolygons;
    }

    private boolean isWaterArea(OsmEntity entity) {
        return OsmDataParser.hasTag(entity, "waterway", "riverbank")
                || OsmDataParser.hasTag(entity, "waterway", "river")
                || OsmDataParser.hasTag(entity, "natural", "water")
                || OsmDataParser.hasTag(entity, "water", "river");
    }

    private void fillExistingWater(DataView view, ShortRasterTile resultTile, RasterCanvas canvas) {
        int width = view.getWidth();
        int height = view.getHeight();
        for (int localZ = 0; localZ < height; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                this.fillExistingWater(view, resultTile, canvas, localX, localZ);
            }
        }
    }

    private void fillExistingWater(DataView view, ShortRasterTile resultTile, RasterCanvas canvas, int localX, int localZ) {
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
}
