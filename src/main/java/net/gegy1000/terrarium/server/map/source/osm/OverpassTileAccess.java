package net.gegy1000.terrarium.server.map.source.osm;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess;

import java.util.HashSet;
import java.util.Set;

public class OverpassTileAccess implements TiledDataAccess {
    private final Set<OverpassSource.Element> elements;
    private final Long2ObjectMap<OverpassSource.Element> nodes;

    public OverpassTileAccess(Set<OverpassSource.Element> elements) {
        this.elements = elements;
        this.nodes = new Long2ObjectOpenHashMap<>();
        this.elements.stream()
                .filter(element -> element.getType().equals("node"))
                .forEach(element -> this.nodes.put(element.getId(), element));
    }

    public OverpassTileAccess() {
        this(new HashSet<>());
    }

    public Set<OverpassSource.Element> getElements() {
        return this.elements;
    }

    public OverpassSource.Element getNode(long id) {
        return this.nodes.get(id);
    }

    public OverpassTileAccess merge(OverpassTileAccess tile) {
        Set<OverpassSource.Element> elements = new HashSet<>(this.elements.size() + tile.elements.size());
        elements.addAll(this.elements);
        elements.addAll(tile.elements);
        return new OverpassTileAccess(elements);
    }
}
