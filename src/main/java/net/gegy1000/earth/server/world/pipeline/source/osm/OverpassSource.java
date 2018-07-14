package net.gegy1000.earth.server.world.pipeline.source.osm;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.pipeline.source.tile.OsmTile;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.DataTilePos;
import net.gegy1000.terrarium.server.world.pipeline.source.SourceResult;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class OverpassSource extends TiledDataSource<OsmTile> {
    private static final double SAMPLE_BUFFER = 5e-4;
    private static final String OVERPASS_ENDPOINT = "http://www.overpass-api.de/api/interpreter";
    private static final OsmTile DEFAULT_TILE = new OsmTile();

    private final int queryVersion;

    private final boolean shouldSample;

    private String query;

    public OverpassSource(CoordinateState latLngCoordinate, double tileSize, String cacheRoot, ResourceLocation queryLocation, int queryVersion) {
        super(new ResourceLocation(TerrariumEarth.MODID, "overpass"), new File(GLOBAL_CACHE_ROOT, cacheRoot), new Coordinate(latLngCoordinate, tileSize, tileSize));
        this.queryVersion = queryVersion;

        this.shouldSample = Math.abs(this.tileSize.getBlockX()) > 512 || Math.abs(this.tileSize.getBlockZ()) > 512;

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
        double minLatitude = this.getLatitude(key) - SAMPLE_BUFFER;
        double minLongitude = this.getLongitude(key) - SAMPLE_BUFFER;
        double maxLatitude = this.getMaxLatitude(key) + SAMPLE_BUFFER;
        double maxLongitude = this.getMaxLongitude(key) + SAMPLE_BUFFER;

        HttpURLConnection connection = (HttpURLConnection) new URL(OVERPASS_ENDPOINT).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("User-Agent", Terrarium.MODID);
        connection.setRequestProperty("Referer", "https://github.com/gegy1000/Terrarium");
        connection.setConnectTimeout(1000);
        connection.setReadTimeout(30000);
        connection.setDoOutput(true);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
            String bbox = String.format("%.6f,%.6f,%.6f,%.6f", minLatitude, minLongitude, maxLatitude, maxLongitude);
            String formattedQuery = this.query.replaceAll(Pattern.quote("{{bbox}}"), bbox);
            writer.write(formattedQuery);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == 429) {
            try {
                // TODO: Handle rate limit better
                Thread.sleep(150);
                return this.getRemoteStream(key);
            } catch (InterruptedException e) {
                Terrarium.LOGGER.error("Interrupted while awaiting rate limit", e);
            }
        }

        return connection.getInputStream();
    }

    @Override
    public InputStream getWrappedStream(InputStream stream) throws IOException {
        return new GZIPInputStream(stream);
    }

    @Override
    public String getCachedName(DataTilePos key) {
        return String.format("%s_%s.osm", key.getTileX(), key.getTileZ());
    }

    @Override
    public OsmTile getDefaultTile() {
        return DEFAULT_TILE;
    }

    @Override
    public SourceResult<OsmTile> parseStream(DataTilePos pos, InputStream stream) throws IOException {
        try {
            return SourceResult.success(OsmDataParser.parse(stream));
        } catch (RuntimeException e) {
            return SourceResult.malformed(e.getMessage());
        }
    }

    @Override
    public void cacheMetadata(DataTilePos key) {
        File metadataFile = new File(this.getCacheRoot(), String.format("%s_%s.meta", key.getTileX(), key.getTileZ()));
        try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metadataFile)))) {
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
}
