package net.gegy1000.earth.server.world.pipeline.source.osm;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OsmJsonIterator implements OsmIterator, OsmHandler {
    private final List<EntityContainer> entities = new ArrayList<>();

    private int cursor = 0;
    private Exception exception = null;

    public OsmJsonIterator(InputStream input) {
        OsmReader reader = new OsmJsonReader(input);
        reader.setHandler(this);

        try {
            reader.read();
        } catch (OsmInputException e) {
            this.exception = e;
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    @Override
    public Iterator<EntityContainer> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        if (this.exception != null) {
            throw new RuntimeException("Error while processing input", this.exception);
        }
        return this.cursor < this.entities.size();
    }

    @Override
    public EntityContainer next() {
        if (!this.hasNext()) {
            throw new IllegalStateException("End of iterator reached");
        }
        return this.entities.get(this.cursor++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from read-only iterator");
    }

    @Override
    public void complete() {
    }

    @Override
    public void handle(OsmBounds bounds) {
    }

    @Override
    public void handle(OsmNode node) {
        this.entities.add(new EntityContainer(EntityType.Node, node));
    }

    @Override
    public void handle(OsmWay way) {
        this.entities.add(new EntityContainer(EntityType.Way, way));
    }

    @Override
    public void handle(OsmRelation relation) {
        this.entities.add(new EntityContainer(EntityType.Relation, relation));
    }

    @Override
    public boolean hasBounds() {
        return false;
    }

    @Override
    public OsmBounds getBounds() {
        return null;
    }
}
