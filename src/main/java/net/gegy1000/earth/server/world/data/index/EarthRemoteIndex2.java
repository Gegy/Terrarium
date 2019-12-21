package net.gegy1000.earth.server.world.data.index;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.source.ElevationSource;
import net.gegy1000.earth.server.world.data.source.SoilSource;
import net.gegy1000.terrarium.server.util.Vec2i;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class EarthRemoteIndex2 {
    public final Zoomable<Endpoint> elevation;
    public final Zoomable<Endpoint> cationExchangeCapacity;
    public final Zoomable<Endpoint> organicCarbonContent;
    public final Zoomable<Endpoint> ph;
    public final Zoomable<Endpoint> clayContent;
    public final Zoomable<Endpoint> siltContent;
    public final Zoomable<Endpoint> sandContent;

    private EarthRemoteIndex2(
            Zoomable<Endpoint> elevation,
            Zoomable<Endpoint> cationExchangeCapacity,
            Zoomable<Endpoint> organicCarbonContent,
            Zoomable<Endpoint> ph,
            Zoomable<Endpoint> clayContent,
            Zoomable<Endpoint> siltContent,
            Zoomable<Endpoint> sandContent
    ) {
        this.elevation = elevation;
        this.cationExchangeCapacity = cationExchangeCapacity;
        this.organicCarbonContent = organicCarbonContent;
        this.ph = ph;
        this.clayContent = clayContent;
        this.siltContent = siltContent;
        this.sandContent = sandContent;
    }

    public static EarthRemoteIndex2 parse(JsonObject root) {
        JsonObject endpointsRoot = root.getAsJsonObject("endpoints");

        return new EarthRemoteIndex2(
                parseZoomableEndpoint(endpointsRoot, "elevation", ElevationSource.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "soil/cec", SoilSource.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "soil/occ", SoilSource.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "soil/ph", SoilSource.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "soil/clay", SoilSource.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "soil/silt", SoilSource.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "soil/sand", SoilSource.zoomLevels())
        );
    }

    private static Zoomable<Endpoint> parseZoomableEndpoint(JsonObject endpointsRoot, String name, ZoomLevels zoomLevels) {
        return Zoomable.create(zoomLevels, zoom -> {
            JsonObject endpointRoot = endpointsRoot.getAsJsonObject(name + "/" + zoom);
            return parseEndpoint(endpointRoot);
        }).orElse(Endpoint.EMPTY);
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
        private static final Endpoint EMPTY = new Endpoint("", Collections.emptyMap());

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
