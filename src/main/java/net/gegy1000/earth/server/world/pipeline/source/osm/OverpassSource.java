package net.gegy1000.earth.server.world.pipeline.source.osm;

import com.google.gson.JsonObject;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTileAccess;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.json.ParseUtils;
import net.gegy1000.terrarium.server.world.pipeline.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceException;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class OverpassSource extends TiledDataSource<OsmTileAccess> implements CachedRemoteSource {
    private static final double SAMPLE_BUFFER = 5e-4;
    private static final String OVERPASS_ENDPOINT = "http://www.overpass-api.de/api/interpreter";

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

    private final File cacheRoot;

    private final int queryVersion;

    private final boolean shouldSample;

    private String query;

    public OverpassSource(CoordinateState latLngCoordinate, double tileSize, String cacheRoot, ResourceLocation queryLocation, int queryVersion) {
        super(new Coordinate(latLngCoordinate, tileSize, tileSize), 4);
        this.cacheRoot = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, cacheRoot);
        this.queryVersion = queryVersion;

        this.shouldSample = this.tileSize.getBlockX() > 512;

        this.loadQuery("/data/" + queryLocation.getResourceDomain() + "/" + queryLocation.getResourcePath());
    }

    private void loadQuery(String queryLocation) {
        InputStream stream = OverpassSource.class.getResourceAsStream(queryLocation);
        if (stream == null) {
            Terrarium.LOGGER.error("Overpass query at {} did not exist", queryLocation);
            return;
        }
        try (BufferedReader input = new BufferedReader(new InputStreamReader(stream))) {
            StringBuilder query = new StringBuilder();
            List<String> lines = IOUtils.readLines(input);
            for (String line : lines) {
                query.append(line);
            }
            this.query = query.toString();
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to load Overpass query at {}", queryLocation, e);
        }
    }

    @Override
    public File getCacheRoot() {
        return this.cacheRoot;
    }

    @Override
    public InputStream getRemoteStream(DataTilePos key) throws IOException {
        HttpPost post = new HttpPost(OVERPASS_ENDPOINT);
        double minLatitude = this.getLatitude(key) - SAMPLE_BUFFER;
        double minLongitude = this.getLongitude(key) - SAMPLE_BUFFER;
        double maxLatitude = this.getMaxLatitude(key) + SAMPLE_BUFFER;
        double maxLongitude = this.getMaxLongitude(key) + SAMPLE_BUFFER;

        String bbox = String.format("%.6f,%.6f,%.6f,%.6f", minLatitude, minLongitude, maxLatitude, maxLongitude);
        String formattedQuery = this.query.replaceAll(Pattern.quote("{{bbox}}"), bbox);
        post.setEntity(new StringEntity(formattedQuery));

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
        return String.format("%s_%s.osm", key.getTileX(), key.getTileZ());
    }

    @Override
    public OsmTileAccess loadTile(DataTilePos key) throws SourceException {
        return this.loadTile(key, 0);
    }

    @Override
    public Class<OsmTileAccess> getTileType() {
        return OsmTileAccess.class;
    }

    private OsmTileAccess loadTile(DataTilePos key, int retries) throws SourceException {
        try {
            return OsmDataParser.parse(this.getStream(key));
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to load overpass map tile at {}", this.getCachedName(key), e);
        } catch (RuntimeException e) {
            Terrarium.LOGGER.error("Failed to parse overpass map tile at {}, reloading", this.getCachedName(key), e);
            this.removeCache(key);
            if (retries < 2) {
                return this.loadTile(key, retries + 1);
            }
        }
        return null;
    }

    @Override
    protected OsmTileAccess getDefaultTile() {
        return new OsmTileAccess();
    }

    @Override
    public void cacheMetadata(DataTilePos key) {
        File metadataFile = new File(this.getCacheRoot(), String.format("%s_%s.meta", key.getTileX(), key.getTileZ()));
        try (DataOutputStream output = new DataOutputStream(new FileOutputStream(metadataFile))) {
            output.writeShort(this.queryVersion);
        } catch (IOException e) {
            Terrarium.LOGGER.error("Failed to cache OSM tile metadata at {}", key, e);
        }
    }

    @Override
    public boolean shouldLoadCache(DataTilePos key, File file) {
        if (file.exists()) {
            File metadataFile = new File(this.getCacheRoot(), String.format("%s_%s.meta", key.getTileX(), key.getTileZ()));
            if (metadataFile.exists()) {
                try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(metadataFile)))) {
                    return input.readUnsignedShort() == this.queryVersion;
                } catch (IOException e) {
                    Terrarium.LOGGER.error("Failed to write OSM tile metadata at {}", key, e);
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldSample() {
        return this.shouldSample;
    }

    private double getLatitude(DataTilePos pos) {
        return pos.getTileX() * this.tileSize.getX();
    }

    private double getLongitude(DataTilePos pos) {
        return pos.getTileZ() * this.tileSize.getZ();
    }

    private double getMaxLatitude(DataTilePos pos) {
        return this.getLatitude(pos) + this.tileSize.getX();
    }

    private double getMaxLongitude(DataTilePos pos) {
        return this.getLongitude(pos) + this.tileSize.getZ();
    }

    public static class Parser implements InstanceObjectParser<TiledDataSource<?>> {
        @Override
        public TiledDataSource<?> parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            String cache = ParseUtils.getString(objectRoot, "cache");
            ResourceLocation queryLocation = new ResourceLocation(ParseUtils.getString(objectRoot, "query"));
            int queryVersion = JsonUtils.getInt(objectRoot, "query_version", 0);
            CoordinateState latLngCoordinate = valueParser.parseCoordinateState(objectRoot, "lat_lng_coordinate");
            double tileSize = ParseUtils.getFloat(objectRoot, "tile_size_latlng");

            return new OverpassSource(latLngCoordinate, tileSize, cache, queryLocation, queryVersion);
        }
    }
}
