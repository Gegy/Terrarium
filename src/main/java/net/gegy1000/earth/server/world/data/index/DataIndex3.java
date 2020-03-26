package net.gegy1000.earth.server.world.data.index;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.util.ZoomLevels;
import net.gegy1000.earth.server.util.Zoomable;
import net.gegy1000.earth.server.world.data.source.ElevationSource;
import net.gegy1000.earth.server.world.data.source.LandCoverSource;
import net.gegy1000.earth.server.world.data.source.SoilSources;
import net.gegy1000.terrarium.server.util.Vec2i;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DataIndex3 {
    public final Zoomable<Endpoint> elevation;
    public final Zoomable<Endpoint> landcover;
    public final Zoomable<Endpoint> cec;
    public final Zoomable<Endpoint> occ;
    public final Zoomable<Endpoint> ph;
    public final Zoomable<Endpoint> clay;
    public final Zoomable<Endpoint> silt;
    public final Zoomable<Endpoint> sand;
    public final Zoomable<Endpoint> usda;

    private DataIndex3(
            Zoomable<Endpoint> elevation,
            Zoomable<Endpoint> landcover,
            Zoomable<Endpoint> cec,
            Zoomable<Endpoint> occ,
            Zoomable<Endpoint> ph,
            Zoomable<Endpoint> clay,
            Zoomable<Endpoint> silt,
            Zoomable<Endpoint> sand,
            Zoomable<Endpoint> usda
    ) {
        this.elevation = elevation;
        this.landcover = landcover;
        this.cec = cec;
        this.occ = occ;
        this.ph = ph;
        this.clay = clay;
        this.sand = sand;
        this.silt = silt;
        this.usda = usda;
    }

    public static DataIndex3 parse(JsonObject root) {
        JsonObject endpointsRoot = root.getAsJsonObject("endpoints");

        return new DataIndex3(
                parseZoomableEndpoint(endpointsRoot, "elevation", ElevationSource.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "landcover", LandCoverSource.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "cec", SoilSources.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "occ", SoilSources.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "ph", SoilSources.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "clay", SoilSources.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "silt", SoilSources.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "sand", SoilSources.zoomLevels()),
                parseZoomableEndpoint(endpointsRoot, "usda", SoilSources.zoomLevels())
        );
    }

    private static Zoomable<Endpoint> parseZoomableEndpoint(JsonObject endpointsRoot, String name, ZoomLevels zoomLevels) {
        return Zoomable.create(zoomLevels, zoom -> {
            String key = name + "/" + zoom;
            if (endpointsRoot.has(key)) {
                JsonObject endpointRoot = endpointsRoot.getAsJsonObject(key);
                return parseEndpoint(endpointRoot);
            } else {
                TerrariumEarth.LOGGER.warn("missing remote endpoint for {}", key);
                return Endpoint.EMPTY;
            }
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
