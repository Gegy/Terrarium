package net.gegy1000.earth.server.world.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.server.world.data.source.TiledDataSource;

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
    private final static String KEYS_URL = "https://gist.githubusercontent.com/gegy1000/07de90970dbb502f9b544481e090a081/raw/terrarium_keys.json";

    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path KEYS_CACHE = TiledDataSource.GLOBAL_CACHE_ROOT.resolve("terrarium_keys.json.gz");

    public static Keys keys = new Keys();

    public static void load() throws IOException {
        try {
            URL url = new URL(KEYS_URL);
            keys = EarthRemoteData.loadKeys(url.openStream());
            EarthRemoteData.cacheKeys(keys);
        } catch (IOException e) {
            TerrariumEarth.LOGGER.error("Failed to load remote Terrarium Earth keys, checking cache {}", e.toString());
            EarthRemoteData.loadCachedKeys();
        }
    }

    private static void loadCachedKeys() throws IOException {
        try (InputStream input = new GZIPInputStream(Files.newInputStream(KEYS_CACHE))) {
            keys = loadKeys(input);
        }
    }

    private static Keys loadKeys(InputStream input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new BufferedInputStream(input))) {
            return GSON.fromJson(reader, Keys.class);
        }
    }

    private static void cacheKeys(Keys keys) {
        try (PrintWriter output = new PrintWriter(new GZIPOutputStream(Files.newOutputStream(KEYS_CACHE)))) {
            output.write(GSON.toJson(keys));
        } catch (IOException e) {
            TerrariumEarth.LOGGER.error("Failed to cache Terrarium Earth info", e);
        }
    }

    public static class Keys {
        @SerializedName("geocoder_key")
        private String geocoderKey = "";
        @SerializedName("autocomplete_key")
        private String autocompleteKey = "";
        @SerializedName("streetview_key")
        private String streetviewKey = "";

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
