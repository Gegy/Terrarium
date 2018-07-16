package net.gegy1000.earth.server.world.pipeline.source.tile;

import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.adt.multicollections.HashMultiSet;
import de.topobyte.adt.multicollections.MultiSet;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityFinder;
import de.topobyte.osm4j.core.resolve.EntityFinders;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.DataView;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.MergableTile;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class OsmTile implements OsmEntityProvider, MergableTile<OsmTile> {
    private final Long2ObjectMap<OsmNode> nodes;
    private final Long2ObjectMap<OsmWay> ways;
    private final Long2ObjectMap<OsmRelation> relations;

    public OsmTile(Long2ObjectMap<OsmNode> nodes, Long2ObjectMap<OsmWay> ways, Long2ObjectMap<OsmRelation> relations) {
        this.nodes = nodes;
        this.ways = ways;
        this.relations = relations;
    }

    public OsmTile() {
        this(new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>());
    }

    public static OsmTile fromDataSet(InMemoryMapDataSet dataSet) {
        return new OsmTile(dataSet.getNodes(), dataSet.getWays(), dataSet.getRelations());
    }

    public Collection<OsmNode> getNodes() {
        return this.nodes.values();
    }

    public Collection<OsmWay> getWays() {
        return this.ways.values();
    }

    public Collection<OsmRelation> getRelations() {
        return this.relations.values();
    }

    @Override
    public OsmTile merge(OsmTile tile) {
        Long2ObjectMap<OsmNode> nodes = new Long2ObjectOpenHashMap<>(this.nodes.size() + tile.nodes.size());
        nodes.putAll(this.nodes);
        nodes.putAll(tile.nodes);

        Long2ObjectMap<OsmWay> ways = new Long2ObjectOpenHashMap<>(this.ways.size() + tile.ways.size());
        ways.putAll(this.ways);
        ways.putAll(tile.ways);

        Long2ObjectMap<OsmRelation> relations = new Long2ObjectOpenHashMap<>(this.relations.size() + tile.relations.size());
        relations.putAll(this.relations);
        relations.putAll(tile.relations);

        return new OsmTile(nodes, ways, relations);
    }

    @Override
    public OsmNode getNode(long id) throws EntityNotFoundException {
        OsmNode node = this.nodes.get(id);
        if (node == null) {
            throw new EntityNotFoundException("Node with id " + id + " not found");
        }
        return node;
    }

    @Override
    public OsmWay getWay(long id) throws EntityNotFoundException {
        OsmWay way = this.ways.get(id);
        if (way == null) {
            throw new EntityNotFoundException("Way with id " + id + " not found");
        }
        return way;
    }

    @Override
    public OsmRelation getRelation(long id) throws EntityNotFoundException {
        OsmRelation relation = this.relations.get(id);
        if (relation == null) {
            throw new EntityNotFoundException("Relation with id " + id + " not found");
        }
        return relation;
    }

    @Override
    public OsmTile copy() {
        Long2ObjectMap<OsmNode> nodes = new Long2ObjectOpenHashMap<>();
        nodes.putAll(this.nodes);
        Long2ObjectMap<OsmWay> ways = new Long2ObjectOpenHashMap<>();
        ways.putAll(this.ways);
        Long2ObjectMap<OsmRelation> relations = new Long2ObjectOpenHashMap<>();
        relations.putAll(this.relations);
        return new OsmTile(nodes, ways, relations);
    }

    public Collection<MultiPolygon> collectPolygons(DataView view, CoordinateState geoCoordinate, Predicate<OsmEntity> filter) {
        double minLatitude = geoCoordinate.getZ(view.getX(), view.getY());
        double minLongitude = geoCoordinate.getX(view.getMaxX(), view.getMaxY());
        double maxLatitude = geoCoordinate.getZ(view.getMaxX(), view.getMaxY());
        double maxLongitude = geoCoordinate.getX(view.getX(), view.getY());
        List<MultiPolygon> polygons = this.getRelations().stream()
                .filter(filter)
                .filter(relation -> this.isRelationInBounds(relation, minLatitude, minLongitude, maxLatitude, maxLongitude))
                .map(relation -> OsmDataParser.createArea(this, relation))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        polygons.addAll(this.getWays().stream()
                .filter(filter)
                .filter(way -> this.isWayInBounds(way, minLatitude, minLongitude, maxLatitude, maxLongitude))
                .map(way -> OsmDataParser.createArea(this, way))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return polygons;
    }

    private boolean isRelationInBounds(OsmRelation relation, double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
        Set<OsmRelation> relations = new HashSet<>();
        MultiSet<OsmWay> ways = new HashMultiSet<>();
        EntityFinder finder = EntityFinders.create(this, EntityNotFoundStrategy.IGNORE);
        relations.add(relation);

        try {
            finder.findMemberRelationsRecursively(relation, relations);
            finder.findMemberWays(relations, ways);
        } catch (EntityNotFoundException e) {
        }

        for (OsmWay way : ways) {
            if (this.isWayInBounds(way, minLatitude, minLongitude, maxLatitude, maxLongitude)) {
                return true;
            }
        }

        return true;
    }

    private boolean isWayInBounds(OsmWay way, double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
        double wayMinLatitude = Double.MAX_VALUE;
        double wayMinLongitude = Double.MAX_VALUE;
        double wayMaxLatitude = -Double.MAX_VALUE;
        double wayMaxLongitude = -Double.MAX_VALUE;

        for (int i = 0; i < way.getNumberOfNodes(); i++) {
            try {
                OsmNode node = this.getNode(way.getNodeId(i));
                double latitude = node.getLatitude();
                if (latitude < wayMinLatitude) {
                    wayMinLatitude = latitude;
                }
                if (latitude > wayMaxLatitude) {
                    wayMaxLatitude = latitude;
                }
                double longitude = node.getLongitude();
                if (longitude < wayMinLongitude) {
                    wayMinLongitude = longitude;
                }
                if (longitude > wayMaxLongitude) {
                    wayMaxLongitude = longitude;
                }
            } catch (EntityNotFoundException e) {
            }
        }

        return minLatitude < wayMaxLatitude && maxLatitude > wayMinLatitude && minLongitude < wayMaxLongitude && maxLongitude > wayMinLongitude;
    }
}
