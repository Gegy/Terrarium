package net.gegy1000.earth.server.world.data.index;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.util.Vec2i;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

// TODO: remove
public final class DataIndex1 {
    public final Endpoint oceans;

    private DataIndex1(Endpoint oceans) {
        this.oceans = oceans;
    }

    public static DataIndex1 parse(JsonObject root) {
        JsonObject endpointsRoot = root.getAsJsonObject("endpoints");
        Endpoint oceans = parseEndpoint(endpointsRoot.getAsJsonObject("ocean"));

        return new DataIndex1(oceans);
    }

    private static Endpoint parseEndpoint(JsonObject root) {
        String url = root.get("url").getAsString();

        JsonArray entryArray = root.getAsJsonArray("entries");
        Map<Vec2i, String> entries = new HashMap<>(entryArray.size());

        for (JsonElement entry : entryArray) {
            parseEntry(entries, entry.getAsJsonObject());
        }

        return new Endpoint(url, entries);
    }

    private static void parseEntry(Map<Vec2i, String> entries, JsonObject root) {
        JsonArray key = root.getAsJsonArray("key");
        String path = root.get("path").getAsString();

        if (key.size() != 2) {
            throw new IllegalStateException("Invalid endpoint JSON: key must have 2 values!");
        }

        int x = key.get(0).getAsInt();
        int y = key.get(1).getAsInt();

        Vec2i pos = new Vec2i(x, y);
        entries.put(pos, path);
    }

    public static class Endpoint {
        private final String url;
        private final Map<Vec2i, String> entries;

        Endpoint(String url, Map<Vec2i, String> entries) {
            this.url = url;
            this.entries = entries;
        }

        public boolean hasEntryFor(Vec2i pos) {
            return this.entries.containsKey(pos);
        }

        @Nullable
        public String getUrlFor(Vec2i pos) {
            String path = this.entries.get(pos);
            if (path == null) {
                return null;
            }
            return this.url + path;
        }
    }
}
