package net.gegy1000.terrarium.server.map.source.osm;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.gegy1000.terrarium.server.map.source.tiled.TiledDataAccess;

import java.util.HashSet;
import java.util.Set;

public class OverpassTileAccess implements TiledDataAccess {
    private final Set<OverpassSource.Element> elements;
    private final Int2ObjectMap<OverpassSource.Element> nodes;

    public OverpassTileAccess(Set<OverpassSource.Element> elements) {
        this.elements = elements;
        this.nodes = new Int2ObjectArrayMap<>();
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

    public OverpassSource.Element getNode(int id) {
        return this.nodes.get(id);
    }
}
