package net.gegy1000.earth.server.world.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class EarthRemoteData {
    private final static String INFO_JSON = "https://gist.githubusercontent.com/gegy1000/0a0ac9ec610d6d9716d43820a0825a6d/raw/terrarium_info.json";

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path INFO_CACHE = TiledDataSource.GLOBAL_CACHE_ROOT.resolve("terrarium_info.json.gz");

    public static EarthRemoteData.Info info = new EarthRemoteData.Info();

    public static void load() throws IOException {
        try {
            URL url = new URL(INFO_JSON);
            info = EarthRemoteData.loadInfo(url.openStream());
            EarthRemoteData.cacheInfo(info);
        } catch (IOException e) {
            TerrariumEarth.LOGGER.error("Failed to load remote Terrarium Earth info, checking cache {}", e.toString());
            EarthRemoteData.loadCachedInfo();
        }
    }

    private static void loadCachedInfo() throws IOException {
        try (InputStream input = new GZIPInputStream(Files.newInputStream(INFO_CACHE))) {
            info = loadInfo(input);
        }
    }

    private static EarthRemoteData.Info loadInfo(InputStream input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(input))) {
            return GSON.fromJson(reader, Info.class);
        }
    }

    private static void cacheInfo(EarthRemoteData.Info info) {
        try (PrintWriter output = new PrintWriter(new GZIPOutputStream(Files.newOutputStream(INFO_CACHE)))) {
            output.write(GSON.toJson(info));
        } catch (IOException e) {
            TerrariumEarth.LOGGER.error("Failed to cache Terrarium Earth info", e);
        }
    }

    public static class Info {
        @SerializedName("raster_map_endpoint")
        private String rasterMapEndpoint = "http://tile.openstreetmap.org";
        @SerializedName("raster_map_query")
        private String rasterMapQuery = "%s/%s%s.png";
        @SerializedName("geocoder_key")
        private String geocoderKey = "";
        @SerializedName("autocomplete_key")
        private String autocompleteKey = "";
        @SerializedName("streetview_key")
        private String streetviewKey = "";

        public String getRasterMapEndpoint() {
            return this.rasterMapEndpoint;
        }

        public String getRasterMapQuery() {
            return this.rasterMapQuery;
        }

        public String getGeocoderKey() {
            byte[] encodedKeyBytes = Base64.getDecoder().decode(this.geocoderKey);
            byte[] decodedBytes = new byte[encodedKeyBytes.length];
            for (int i = 0; i < encodedKeyBytes.length; i++) {
                decodedBytes[i] = (byte) (encodedKeyBytes[i] - (i << i) - 31);
            }
            return new String(decodedBytes);
        }

        public String getAutocompleteKey() {
            byte[] encodedKeyBytes = Base64.getDecoder().decode(this.autocompleteKey);
            byte[] decodedBytes = new byte[encodedKeyBytes.length];
            for (int i = 0; i < encodedKeyBytes.length; i++) {
                decodedBytes[i] = (byte) (encodedKeyBytes[i] - (i << i) - 961);
            }
            return new String(decodedBytes);
        }

        public String getStreetviewKey() {
            byte[] encodedKeyBytes = Base64.getDecoder().decode(this.streetviewKey);
            byte[] decodedBytes = new byte[encodedKeyBytes.length];
            for (int i = 0; i < encodedKeyBytes.length; i++) {
                decodedBytes[i] = (byte) (encodedKeyBytes[i] - (i << i) - 729);
            }
            return new String(decodedBytes);
        }
    }
}
