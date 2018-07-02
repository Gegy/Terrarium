package net.gegy1000.earth.server.world.pipeline.source.tile;

import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;

public class OsmTile implements OsmEntityProvider, TiledDataAccess {
    private final TLongObjectMap<OsmNode> nodes;
    private final TLongObjectMap<OsmWay> ways;

    public OsmTile(TLongObjectMap<OsmNode> nodes, TLongObjectMap<OsmWay> ways) {
        this.nodes = nodes;
        this.ways = ways;
    }

    public OsmTile(InMemoryMapDataSet data) {
        this(data.getNodes(), data.getWays());
    }

    public OsmTile() {
        this(new TLongObjectHashMap<>(), new TLongObjectHashMap<>());
    }

    public TLongObjectMap<OsmNode> getNodes() {
        return this.nodes;
    }

    public TLongObjectMap<OsmWay> getWays() {
        return this.ways;
    }

    public OsmTile merge(OsmTile tile) {
        TLongObjectMap<OsmNode> nodes = new TLongObjectHashMap<>(this.nodes.size() + tile.nodes.size());
        nodes.putAll(this.nodes);
        nodes.putAll(tile.nodes);

        TLongObjectMap<OsmWay> ways = new TLongObjectHashMap<>(this.ways.size() + tile.ways.size());
        ways.putAll(this.ways);
        ways.putAll(tile.ways);

        return new OsmTile(nodes, ways);
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
        throw new EntityNotFoundException("Relation with id " + id + " not found");
    }
}
