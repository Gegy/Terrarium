package net.gegy1000.terrarium.server.map.source.osm;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.map.osm.OsmDataParser;
import net.gegy1000.terrarium.server.map.source.CachedRemoteSource;
import net.gegy1000.terrarium.server.map.source.SourceException;
import net.gegy1000.terrarium.server.map.source.tiled.DataTilePos;
import net.gegy1000.terrarium.server.map.source.tiled.TiledSource;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
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

public class OverpassSource extends TiledSource<OverpassTileAccess> implements CachedRemoteSource {
    private static final File CACHE_ROOT = new File(CachedRemoteSource.GLOBAL_CACHE_ROOT, "osm");

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

    private final EarthGenerationSettings settings;

    private final File cacheRoot;

    private final String queryLocation;
    private final int queryVersion;

    private final boolean shouldSample;

    private String query;

    public OverpassSource(EarthGenerationSettings settings, double tileSize, String cacheRoot, String queryLocation, int queryVersion) {
        super(tileSize, 4);
        this.settings = settings;
        this.cacheRoot = new File(CACHE_ROOT, cacheRoot);
        this.queryLocation = queryLocation;
        this.queryVersion = queryVersion;

        this.shouldSample = Coordinate.fromLatLng(settings, tileSize, tileSize).getBlockX() > 512;
    }

    public void loadQuery() {
        String queryLocation = this.queryLocation;
        try (BufferedReader input = new BufferedReader(new InputStreamReader(OverpassSource.class.getResourceAsStream(queryLocation)))) {
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
        double minLatitude = this.getLatitude(key) - 0.0005;
        double minLongitude = this.getLongitude(key) - 0.0005;
        double maxLatitude = this.getMaxLatitude(key) + 0.0005;
        double maxLongitude = this.getMaxLongitude(key) + 0.0005;

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
        return String.format("%s_%s.osm", key.getTileX(), key.getTileY());
    }

    @Override
    public OverpassTileAccess loadTile(DataTilePos key) throws SourceException {
        return this.loadTile(key, 0);
    }

    private OverpassTileAccess loadTile(DataTilePos key, int retries) throws SourceException {
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
            output.writeShort(this.queryVersion);
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
                    return input.readUnsignedShort() == this.queryVersion;
                } catch (IOException e) {
                    Terrarium.LOGGER.error("Failed to write OSM tile metadata at {}", key, e);
                }
            }
        }
        return false;
    }

    public boolean shouldSample() {
        return this.shouldSample;
    }

    private double getLatitude(DataTilePos pos) {
        return -pos.getTileY() * this.getTileSize();
    }

    private double getLongitude(DataTilePos pos) {
        return pos.getTileX() * this.getTileSize();
    }

    private double getMaxLatitude(DataTilePos pos) {
        return this.getLatitude(pos) + this.getTileSize();
    }

    private double getMaxLongitude(DataTilePos pos) {
        return this.getLongitude(pos) + this.getTileSize();
    }
}
