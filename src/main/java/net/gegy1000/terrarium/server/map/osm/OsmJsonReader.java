package net.gegy1000.terrarium.server.map.osm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Entity;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.impl.RelationMember;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.impl.Way;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OsmJsonReader implements OsmReader {
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final InputStream input;

    private OsmHandler handler;

    public OsmJsonReader(InputStream input) {
        this.input = input;
    }

    @Override
    public void setHandler(OsmHandler handler) {
        this.handler = handler;
    }

    @Override
    public void read() throws OsmInputException {
        if (this.handler == null) {
            throw new OsmInputException("handler not set");
        }

        try (InputStreamReader reader = new InputStreamReader(this.input)) {
            JsonObject root = JSON_PARSER.parse(reader).getAsJsonObject();
            JsonArray elementsArray = root.getAsJsonArray("elements");

            for (JsonElement element : elementsArray) {
                this.parseElement(element.getAsJsonObject());
            }
        } catch (IOException e) {
            throw new OsmInputException("error while parsing json data", e);
        }

        try {
            this.handler.complete();
        } catch (IOException e) {
            throw new OsmInputException("error while completing handler", e);
        }
    }

    private void parseElement(JsonObject elementObject) throws IOException {
        long id = elementObject.get("id").getAsLong();
        String type = elementObject.get("type").getAsString();

        switch (type) {
            case "node":
                this.parseNode(id, elementObject);
                break;
            case "way":
                this.parseWay(id, elementObject);
                break;
            case "relation":
                this.parseRelation(id, elementObject);
                break;
        }
    }

    private void parseNode(long id, JsonObject elementObject) throws IOException {
        double latitude = elementObject.get("lat").getAsDouble();
        double longitude = elementObject.get("lon").getAsDouble();

        Node node = new Node(id, latitude, longitude);
        this.parseTags(node, elementObject);

        this.handler.handle(node);
    }

    private void parseWay(long id, JsonObject elementObject) throws IOException {
        TLongList nodes = new TLongArrayList();

        if (elementObject.has("nodes")) {
            JsonArray nodesArray = elementObject.get("nodes").getAsJsonArray();
            for (JsonElement nodeElement : nodesArray) {
                nodes.add(nodeElement.getAsLong());
            }
        }

        Way way = new Way(id, nodes);
        this.parseTags(way, elementObject);

        this.handler.handle(way);
    }

    private void parseRelation(long id, JsonObject elementObject) throws IOException {
        List<OsmRelationMember> members = new ArrayList<>();

        if (elementObject.has("members")) {
            JsonArray membersArray = elementObject.getAsJsonArray("members");
            for (JsonElement memberElement : membersArray) {
                JsonObject memberObject = memberElement.getAsJsonObject();

                long ref = memberObject.get("ref").getAsLong();
                EntityType type = this.parseType(memberObject.get("type").getAsString());
                String role = memberObject.get("role").getAsString();

                members.add(new RelationMember(ref, type, role));
            }
        }

        Relation relation = new Relation(id, members);
        this.parseTags(relation, elementObject);

        this.handler.handle(relation);
    }

    private EntityType parseType(String type) {
        switch (type) {
            case "node":
                return EntityType.Node;
            case "way":
                return EntityType.Way;
            case "relation":
                return EntityType.Relation;
        }
        return null;
    }

    private void parseTags(Entity entity, JsonObject elementObject) {
        List<OsmTag> tags = new ArrayList<>();

        if (elementObject.has("tags")) {
            JsonObject tagsObject = elementObject.getAsJsonObject("tags");
            for (Map.Entry<String, JsonElement> entry : tagsObject.entrySet()) {
                tags.add(new Tag(entry.getKey(), entry.getValue().getAsString()));
            }
        }

        entity.setTags(tags);
    }
}
