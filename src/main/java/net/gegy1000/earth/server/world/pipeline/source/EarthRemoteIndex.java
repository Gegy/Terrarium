package net.gegy1000.earth.server.world.pipeline.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import org.apache.commons.io.IOUtils;
import org.tukaani.xz.SingleXZInputStream;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// TODO: Load from GUI, blocking mod usage if unable to load
public final class EarthRemoteIndex {
    private final static String INDEX_URL = "https://terrariumearth.azureedge.net/geo/data_index.json.xz";
    private final static String SHA1_URL = "https://terrariumearth.azureedge.net/geo/data_index.json.xz.sha1";

    private static final Path CACHE_PATH = TiledDataSource.GLOBAL_CACHE_ROOT.resolve("remote_index.json.xz");
    private final static JsonParser JSON_PARSER = new JsonParser();

    private static EarthRemoteIndex index = null;

    public final Endpoint srtm;
    public final Endpoint landcover;
    public final Endpoint oceans;

    private EarthRemoteIndex(Endpoint srtm, Endpoint landcover, Endpoint oceans) {
        this.srtm = srtm;
        this.landcover = landcover;
        this.oceans = oceans;
    }

    public static EarthRemoteIndex get() {
        if (index == null) {
            throw new IllegalStateException("Remote index not yet loaded!");
        }
        return index;
    }

    public static void load() throws IOException {
        index = loadIndex();
    }

    private static EarthRemoteIndex loadIndex() throws IOException {
        if (Files.exists(CACHE_PATH)) {
            byte[] cachedBytes = Files.readAllBytes(CACHE_PATH);
            if (isCacheUpToDate(cachedBytes)) {
                return parse(cachedBytes);
            }
        }

        URL url = new URL(INDEX_URL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", TerrariumEarth.USER_AGENT);

        try (InputStream input = connection.getInputStream()) {
            byte[] bytes = IOUtils.toByteArray(input);

            try (OutputStream output = Files.newOutputStream(CACHE_PATH)) {
                output.write(bytes);
            }

            return parse(bytes);
        }
    }

    private static boolean isCacheUpToDate(byte[] cachedBytes) {
        try {
            byte[] remoteHash = loadIndexHash();

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] cachedHash = sha1.digest(cachedBytes);

            return Arrays.equals(cachedHash, remoteHash);
        } catch (IOException | NoSuchAlgorithmException e) {
            TerrariumEarth.LOGGER.warn("Failed to compare cache hash", e);
            return true;
        }
    }

    private static EarthRemoteIndex parse(byte[] bytes) throws IOException {
        try (InputStream input = new SingleXZInputStream(new ByteArrayInputStream(bytes))) {
            JsonElement root = JSON_PARSER.parse(new InputStreamReader(input));
            return parseJson(root.getAsJsonObject());
        }
    }

    private static EarthRemoteIndex parseJson(JsonObject root) {
        JsonObject endpointsRoot = root.getAsJsonObject("endpoints");

        Endpoint srtm = parseEndpoint(endpointsRoot.getAsJsonObject("srtm"));
        Endpoint landcover = parseEndpoint(endpointsRoot.getAsJsonObject("landcover"));
        Endpoint oceans = parseEndpoint(endpointsRoot.getAsJsonObject("ocean"));

        return new EarthRemoteIndex(srtm, landcover, oceans);
    }

    private static Endpoint parseEndpoint(JsonObject root) {
        String url = root.get("url").getAsString();

        JsonArray entryArray = root.getAsJsonArray("entries");
        Map<DataTilePos, String> entries = new HashMap<>(entryArray.size());

        for (JsonElement entry : entryArray) {
            parseEntry(entries, entry.getAsJsonObject());
        }

        return new Endpoint(url, entries);
    }

    private static void parseEntry(Map<DataTilePos, String> entries, JsonObject root) {
        JsonArray key = root.getAsJsonArray("key");
        String path = root.get("path").getAsString();

        if (key.size() != 2) {
            throw new IllegalStateException("Invalid endpoint JSON: key must have 2 values!");
        }

        int x = key.get(0).getAsInt();
        int y = key.get(1).getAsInt();

        DataTilePos pos = new DataTilePos(x, y);
        entries.put(pos, path);
    }

    private static byte[] loadIndexHash() throws IOException {
        URL url = new URL(SHA1_URL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", TerrariumEarth.USER_AGENT);

        try (InputStream input = connection.getInputStream()) {
            return IOUtils.toByteArray(input);
        }
    }

    public static class Endpoint {
        private final String url;
        private final Map<DataTilePos, String> entries;

        Endpoint(String url, Map<DataTilePos, String> entries) {
            this.url = url;
            this.entries = entries;
        }

        public boolean hasEntryFor(DataTilePos pos) {
            return this.entries.containsKey(pos);
        }

        @Nullable
        public String getUrlFor(DataTilePos pos) {
            String path = this.entries.get(pos);
            if (path == null) {
                return null;
            }
            return this.url + path;
        }
    }
}
