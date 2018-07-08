package net.gegy1000.earth.server.world.pipeline.source.tile;

import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.MergableTile;

import java.util.Collection;

public class OsmTile implements OsmEntityProvider, MergableTile<OsmTile> {
    private final TLongObjectMap<OsmNode> nodes;
    private final TLongObjectMap<OsmWay> ways;
    private final TLongObjectMap<OsmRelation> relations;

    public OsmTile(TLongObjectMap<OsmNode> nodes, TLongObjectMap<OsmWay> ways, TLongObjectMap<OsmRelation> relations) {
        this.nodes = nodes;
        this.ways = ways;
        this.relations = relations;
    }

    public OsmTile() {
        this(new TLongObjectHashMap<>(), new TLongObjectHashMap<>(), new TLongObjectHashMap<>());
    }

    public static OsmTile fromDataSet(InMemoryMapDataSet dataSet) {
        return new OsmTile(dataSet.getNodes(), dataSet.getWays(), dataSet.getRelations());
    }

    public Collection<OsmNode> getNodes() {
        return this.nodes.valueCollection();
    }

    public Collection<OsmWay> getWays() {
        return this.ways.valueCollection();
    }

    public Collection<OsmRelation> getRelations() {
        return this.relations.valueCollection();
    }

    @Override
    public OsmTile merge(OsmTile tile) {
        TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>(this.nodes.size() + tile.nodes.size());
        nodes.putAll(this.nodes);
        nodes.putAll(tile.nodes);

        TLongObjectMap<OsmWay> ways = new TLongObjectHashMap<>(this.ways.size() + tile.ways.size());
        ways.putAll(this.ways);
        ways.putAll(tile.ways);

        TLongObjectMap<OsmRelation> relations = new TLongObjectHashMap<>(this.relations.size() + tile.relations.size());
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
        TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>();
        nodes.putAll(this.nodes);
        TLongObjectMap<OsmWay> ways = new TLongObjectHashMap<>();
        ways.putAll(this.ways);
        TLongObjectMap<OsmRelation> relations = new TLongObjectHashMap<>();
        relations.putAll(this.relations);
        return new OsmTile(nodes, ways, relations);
    }
}
