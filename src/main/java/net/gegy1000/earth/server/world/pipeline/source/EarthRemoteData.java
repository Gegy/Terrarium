package net.gegy1000.earth.server.world.pipeline.source;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class EarthRemoteData {
    private final static String INFO_JSON = "https://gist.githubusercontent.com/gegy1000/0a0ac9ec610d6d9716d43820a0825a6d/raw/terrarium_info.json";

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final File INFO_CACHE = new File(TiledDataSource.GLOBAL_CACHE_ROOT, "terrarium_info.json.gz");

    public static EarthRemoteData.Info info = new EarthRemoteData.Info();

    public static void loadInfo() throws IOException {
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
        try (InputStream input = new GZIPInputStream(new FileInputStream(INFO_CACHE))) {
            info = loadInfo(input);
        }
    }

    private static EarthRemoteData.Info loadInfo(InputStream input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(input))) {
            return GSON.fromJson(reader, Info.class);
        }
    }

    private static void cacheInfo(EarthRemoteData.Info info) {
        try (PrintWriter output = new PrintWriter(new GZIPOutputStream(new FileOutputStream(INFO_CACHE)))) {
            output.write(GSON.toJson(info));
        } catch (IOException e) {
            TerrariumEarth.LOGGER.error("Failed to cache Terrarium Earth info", e);
        }
    }

    public static class Info {
        @SerializedName("base_url")
        private String baseURL = "";
        @SerializedName("landcover_endpoint")
        private String landcoverEndpoint = "";
        @SerializedName("landcover_query")
        private String landcoverQuery = "%s_%s.lc";
        @SerializedName("soil_endpoint")
        private String soilEndpoint = "";
        @SerializedName("soil_query")
        private String soilQuery = "%s_%s.sc";
        @SerializedName("ocean_endpoint")
        private String oceanEndpoint = "";
        @SerializedName("ocean_query")
        private String oceanQuery = "%s_%s.water";
        @SerializedName("heights_endpoint")
        private String heightsEndpoint = "";
        @SerializedName("heights_query")
        private String heightsQuery = "%s_%s.ht2";
        @SerializedName("height_tiles")
        private String heightTiles = "";
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

        public String getBaseURL() {
            return this.baseURL;
        }

        public String getLandcoverEndpoint() {
            return this.landcoverEndpoint;
        }

        public String getLandcoverQuery() {
            return this.landcoverQuery;
        }

        public String getSoilEndpoint() {
            return this.soilEndpoint;
        }

        public String getSoilQuery() {
            return this.soilQuery;
        }

        public String getOceanEndpoint() {
            return this.oceanEndpoint;
        }

        public String getOceanQuery() {
            return this.oceanQuery;
        }

        public String getHeightsEndpoint() {
            return this.heightsEndpoint;
        }

        public String getHeightsQuery() {
            return this.heightsQuery;
        }

        public String getHeightTiles() {
            return this.heightTiles;
        }

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
