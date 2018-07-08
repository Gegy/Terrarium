package net.gegy1000.earth.server.world.pipeline.source.osm;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy;
import de.topobyte.osm4j.geometry.NodeBuilder;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.geometry.RegionBuilderResult;
import de.topobyte.osm4j.geometry.WayBuilder;
import de.topobyte.osm4j.geometry.WayBuilderResult;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OsmDataParser {
    private static final RegionBuilder REGION_BUILDER = new RegionBuilder();
    private static final WayBuilder WAY_BUILDER = new WayBuilder();
    private static final NodeBuilder NODE_BUILDER = new NodeBuilder();

    static {
        REGION_BUILDER.setMissingEntitiesStrategy(MissingEntitiesStrategy.BUILD_PARTIAL);
        WAY_BUILDER.setMissingEntitiesStrategy(MissingEntitiesStrategy.BUILD_PARTIAL);
    }

    public static OsmTile parse(InputStream input) throws IOException {
        InMemoryMapDataSet entities = OsmDataParser.parseEntities(input);
        return OsmTile.fromDataSet(entities);
    }

    private static InMemoryMapDataSet parseEntities(InputStream input) throws IOException {
        try {
            OsmIterator iterator = new OsmJsonIterator(input);
            return MapDataSetLoader.read(iterator, true, true, true);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public static boolean hasTag(OsmEntity entity, String key, String value) {
        for (int i = 0; i < entity.getNumberOfTags(); i++) {
            OsmTag tag = entity.getTag(i);
            if (tag.getKey().equals(key) && tag.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static List<LineString> createLines(OsmEntityProvider data, OsmWay way) {
        List<LineString> results = new ArrayList<>();
        try {
            WayBuilderResult lines = WAY_BUILDER.build(way, data);
            results.addAll(lines.getLineStrings());
            if (lines.getLinearRing() != null) {
                results.add(lines.getLinearRing());
            }
        } catch (EntityNotFoundException e) {
            Terrarium.LOGGER.warn("Unable to find way node", e);
        }
        return results;
    }

    public static MultiPolygon createArea(OsmEntityProvider data, OsmWay way) {
        try {
            RegionBuilderResult region = REGION_BUILDER.build(way, data);
            MultiPolygon multiPolygon = region.getMultiPolygon();
            if (multiPolygon.isEmpty()) {
                return null;
            }
            return multiPolygon;
        } catch (EntityNotFoundException e) {
            Terrarium.LOGGER.warn("Unable to find relation member", e);
            return null;
        }
    }

    public static MultiPolygon createArea(OsmEntityProvider data, OsmRelation relation) {
        try {
            RegionBuilderResult region = REGION_BUILDER.build(relation, data);
            MultiPolygon multiPolygon = region.getMultiPolygon();
            if (multiPolygon.isEmpty()) {
                return null;
            }
            return multiPolygon;
        } catch (EntityNotFoundException e) {
            Terrarium.LOGGER.warn("Unable to find relation member", e);
            return null;
        }
    }

    public static List<Point> createPoints(OsmEntityProvider data, OsmWay way) {
        List<Point> points = new ArrayList<>();
        try {
            for (int i = 0; i < way.getNumberOfNodes(); i++) {
                points.add(NODE_BUILDER.build(data.getNode(way.getNodeId(i))));
            }
        } catch (EntityNotFoundException e) {
            Terrarium.LOGGER.warn("Unable to find node", e);
        }
        return points;
    }
}
