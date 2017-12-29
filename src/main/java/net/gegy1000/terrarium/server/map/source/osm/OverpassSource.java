package net.gegy1000.terrarium.server.map.source.osm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.map.source.SourceException;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OverpassSource extends TiledSource<OverpassTileAccess> implements CachedRemoteSource {
    public static final double TILE_SIZE_DEGREES = 0.2;

    private static final File CACHE_ROOT = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, "osm");

    private static final String OVERPASS_ENDPOINT = "http://www.overpass-api.de/api/interpreter";
    private static final String QUERY_LOCATION = "/assets/terrarium/query/overpass_query.oql";

    private static final int QUERY_VERSION = 1;

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final CloseableHttpClient client = HttpClientBuilder.create()
            .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                request.setHeader("Accent-Encoding", "gzip");
                request.setHeader("User-Agent", Terrarium.MODID);
                request.setHeader("Referer", "https://github.com/gegy1000/Terrarium");
            }).addInterceptorFirst((HttpResponseInterceptor) (response, context) -> {
                HttpEntity entity = response.getEntity();
                Arrays.stream(entity.getContentEncoding().getElements())
                        .filter(element -> element.getName().equalsIgnoreCase("gzip"))
                        .findFirst()
                        .ifPresent(element -> response.setEntity(new GzipDecompressingEntity(entity)));
            })
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(1000)
                    .setConnectionRequestTimeout(1000)
                    .setSocketTimeout(30000)
                    .build())
            .build();

    private final EarthGenerationSettings settings;

    private String query;

    public OverpassSource(EarthGenerationSettings settings) {
        super(TILE_SIZE_DEGREES, 4);
        this.settings = settings;
    }

    public void loadQuery() {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(OverpassSource.class.getResourceAsStream(QUERY_LOCATION)))) {
            StringBuilder query = new StringBuilder();
            List<String> lines = IOUtils.readLines(input);
            for (String line : lines) {
                query.append(line);
            }
            this.query = query.toString();
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to load Overpass query", e);
        }
    }

    @Override
    public File getCacheRoot() {
        return CACHE_ROOT;
    }

    public OverpassTileAccess sampleArea(Coordinate minCoordinate, Coordinate maxCoordinate) {
        DataTilePos minTilePos = this.getTilePos(minCoordinate);
        DataTilePos maxTilePos = this.getTilePos(maxCoordinate);

        Set<Element> elements = new HashSet<>();

        for (int tileZ = minTilePos.getTileY(); tileZ <= maxTilePos.getTileY(); tileZ++) {
            for (int tileX = minTilePos.getTileX(); tileX <= maxTilePos.getTileX(); tileX++) {
                OverpassTileAccess tile = this.getTile(new DataTilePos(tileX, tileZ));
                if (tile != null) {
                    elements.addAll(tile.getElements());
                }
            }
        }

        return new OverpassTileAccess(elements);
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        HttpPost post = new HttpPost(OVERPASS_ENDPOINT);
        double minLatitude = this.getLatitude(key);
        double minLongitude = this.getLongitude(key);
        double maxLatitude = this.getMaxLatitude(key);
        double maxLongitude = this.getMaxLongitude(key);
        post.setEntity(new StringEntity(String.format(this.query, minLatitude, minLongitude, maxLatitude, maxLongitude)));

        CloseableHttpResponse response = this.client.execute(post);
        if (response.getStatusLine().getStatusCode() == 429) {
            try {
                // TODO: Handle rate limit better
                response.close();
                Thread.sleep(150);
                return this.getRemoteStream(key);
            } catch (InterruptedException e) {
                Terrarium.LOGGER.error("Interrupted while awaiting rate limit", e);
            }
        }

        return response.getEntity().getContent();
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format("%s_%s.osm", key.getTileX(), key.getTileY());
    }

    @Override
    public OverpassTileAccess loadTile(DataTilePos key) throws SourceException {
        return this.loadTile(key, 0);
    }

    private OverpassTileAccess loadTile(DataTilePos key, int retries) throws SourceException {
        try (InputStreamReader input = new InputStreamReader(this.getStream(key))) {
            Set<Element> elements = new HashSet<>();

            JsonObject root = JSON_PARSER.parse(input).getAsJsonObject();
            JsonArray elementsArray = root.getAsJsonArray("elements");

            for (JsonElement element : elementsArray) {
                JsonObject elementObject = element.getAsJsonObject();

                Map<String, String> tags = new HashMap<>();
                IntList nodes = new IntArrayList();

                int id = elementObject.get("id").getAsInt();
                String type = elementObject.get("type").getAsString();

                double latitude = elementObject.has("lat") ? elementObject.get("lat").getAsDouble() : 0.0;
                double longitude = elementObject.has("lon") ? elementObject.get("lon").getAsDouble() : 0.0;

                if (elementObject.has("tags")) {
                    JsonObject tagsObject = elementObject.getAsJsonObject("tags");
                    for (Map.Entry<String, JsonElement> entry : tagsObject.entrySet()) {
                        tags.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }

                if (elementObject.has("nodes")) {
                    JsonArray nodesArray = elementObject.get("nodes").getAsJsonArray();
                    for (JsonElement nodeElement : nodesArray) {
                        nodes.add(nodeElement.getAsInt());
                    }
                }

                elements.add(new Element(id, type, latitude, longitude, nodes, tags));
            }

            return new OverpassTileAccess(elements);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to load overpass map tile at {}", this.getCachedName(key), e);
        } catch (JsonParseException e) {
            Terrarium.LOGGER.error("Failed to parse overpass map tile at {}, reloading", this.getCachedName(key), e);
            this.removeCache(key);
            if (retries < 2) {
                return this.loadTile(key, retries + 1);
            }
        }

        return null;
    }

    @Override
    protected OverpassTileAccess getDefaultTile() {
        return new OverpassTileAccess();
    }

    @Override
    public EarthGenerationSettings getSettings() {
        return this.settings;
    }

    @Override
    public void cacheMetadata(DataTilePos key) {
        File metadataFile = new File(this.getCacheRoot(), String.format("%s_%s.meta", key.getTileX(), key.getTileY()));
        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(metadataFile))) {
            output.writeShort(QUERY_VERSION);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to cache OSM tile metadata at {}", key, e);
        }
    }

    @Override
    public boolean shouldLoadCache(DataTilePos key, File file) {
        if (file.exists()) {
            File metadataFile = new File(this.getCacheRoot(), String.format("%s_%s.meta", key.getTileX(), key.getTileY()));
            if (metadataFile.exists()) {
                try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(metadataFile)))) {
                    return input.readUnsignedShort() == QUERY_VERSION;
                } catch (IOException e) {
                    Terrarium.LOGGER.error("Failed to write OSM tile metadata at {}", key, e);
                }
            }
        }
        return false;
    }

    private DataTilePos getTilePos(Coordinate coordinate) {
        int tileX = MathHelper.floor(coordinate.getLongitude() / OverpassSource.TILE_SIZE_DEGREES);
        int tileZ = MathHelper.ceil(-coordinate.getLatitude() / OverpassSource.TILE_SIZE_DEGREES);
        return new DataTilePos(tileX, tileZ);
    }

    private double getLatitude(DataTilePos pos) {
        return -pos.getTileY() * OverpassSource.TILE_SIZE_DEGREES;
    }

    private double getLongitude(DataTilePos pos) {
        return pos.getTileX() * OverpassSource.TILE_SIZE_DEGREES;
    }

    private double getMaxLatitude(DataTilePos pos) {
        return this.getLatitude(pos) + OverpassSource.TILE_SIZE_DEGREES;
    }

    private double getMaxLongitude(DataTilePos pos) {
        return this.getLongitude(pos) + OverpassSource.TILE_SIZE_DEGREES;
    }

    public static class Element {
        private final int id;
        private final String type;
        private final double latitude;
        private final double longitude;
        private final IntList nodes;
        private final Map<String, String> tags;

        public Element(int id, String type, double latitude, double longitude, IntList nodes, Map<String, String> tags) {
            this.id = id;
            this.type = type;
            this.latitude = latitude;
            this.longitude = longitude;
            this.nodes = nodes;
            this.tags = tags;
        }

        public int getId() {
            return this.id;
        }

        public String getType() {
            return this.type;
        }

        public double getLatitude() {
            return this.latitude;
        }

        public double getLongitude() {
            return this.longitude;
        }

        public IntList getNodes() {
            return this.nodes;
        }

        public List<Element> collectNodes(OverpassTileAccess nodeAccess) {
            List<Element> nodes = new ArrayList<>(this.nodes.size());
            for (int id : this.nodes) {
                nodes.add(nodeAccess.getNode(id));
            }
            return nodes;
        }

        public Map<String, String> getTags() {
            return this.tags;
        }

        public boolean isType(String key, String value) {
            return value.equals(this.tags.get(key));
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Element && ((Element) obj).id == this.id;
        }

        @Override
        public int hashCode() {
            return this.id * 31;
        }
    }
}
