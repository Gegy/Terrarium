package net.gegy1000.earth.server.world.pipeline.source.tile;

import com.vividsolutions.jts.geom.MultiPolygon;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.earth.server.world.pipeline.source.osm.OsmDataParser;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.MergableTile;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
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

    public Collection<MultiPolygon> collectPolygons(Predicate<OsmEntity> filter) {
        List<MultiPolygon> polygons = this.getRelations().stream()
                .filter(filter)
                .map(relation -> OsmDataParser.createArea(this, relation))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        polygons.addAll(this.getWays().stream()
                .filter(filter)
                .map(way -> OsmDataParser.createArea(this, way))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        return polygons;
    }
}
