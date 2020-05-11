package net.gegy1000.earth.server.world.data;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.earth.server.shared.SharedEarthData;
import org.apache.http.HttpHeaders;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class GooglePanorama {
    private static final String METADATA_ADDRESS = "https://maps.googleapis.com/maps/api/streetview/metadata?location=%.5f,%.5f&radius=%.0f&source=outdoor&key=%s";
    private static final String PANORAMA_ADDRESS = "https://geo0.ggpht.com/cbk?cb_client=maps_sv.tactile&panoid=%s&output=tile&x=%s&y=%s&zoom=%s&nbt&fover=2";

    private static final String BROWSER_UA = "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/61.0";

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final String id;
    private final double latitude;
    private final double longitude;

    GooglePanorama(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Nullable
    public static GooglePanorama lookup(double latitude, double longitude, double radius) throws IOException {
        EarthApiKeys keys = SharedEarthData.instance().get(SharedEarthData.API_KEYS);
        if (keys == null) return null;

        String key = keys.getStreetviewKey();
        if (Strings.isNullOrEmpty(key)) return null;

        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(METADATA_ADDRESS, latitude, longitude, radius, key)).openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty(HttpHeaders.USER_AGENT, "terrarium");
        connection.setRequestProperty(HttpHeaders.REFERER, "https://github.com/gegy1000/Terrarium");

        connection.setConnectTimeout(4000);
        connection.setReadTimeout(30000);

        try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(connection.getInputStream()))) {
            JsonObject root = (JsonObject) JSON_PARSER.parse(input);

            handleResponseStatus(root);

            if (root.has("location")) {
                JsonObject location = root.getAsJsonObject("location");
                double panoLat = location.get("lat").getAsDouble();
                double panoLon = location.get("lng").getAsDouble();

                String panoId = root.get("pano_id").getAsString();

                return new GooglePanorama(panoId, panoLat, panoLon);
            }
        }

        return null;
    }

    private static void handleResponseStatus(JsonObject root) throws IOException {
        if (root.has("status")) {
            String status = root.get("status").getAsString();
            if (status.equalsIgnoreCase("OVER_QUERY_LIMIT")) {
                throw new IOException("Reached query limit for Google API! Try again in a few minutes");
            } else if (status.equalsIgnoreCase("REQUEST_DENIED")) {
                throw new IOException(root.get("error_message").getAsString());
            }
        }
    }

    public String getId() {
        return this.id;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public BufferedImage loadTile(int tileX, int tileY, int zoom) throws IOException {
        URL url = new URL(String.format(PANORAMA_ADDRESS, this.id, tileX, tileY, zoom));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty(HttpHeaders.ACCEPT, "*/*");
        connection.setRequestProperty(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5");
        connection.setRequestProperty(HttpHeaders.CONNECTION, "keep-alive");
        connection.setRequestProperty(HttpHeaders.HOST, "geo0.ggpht.com");
        connection.setRequestProperty("Origin", "https://www.google.com");
        connection.setRequestProperty(HttpHeaders.REFERER, "https://www.google.com/");
        connection.setRequestProperty(HttpHeaders.USER_AGENT, BROWSER_UA);

        connection.setConnectTimeout(4000);
        connection.setReadTimeout(30000);

        try (InputStream input = new BufferedInputStream(connection.getInputStream())) {
            return ImageIO.read(input);
        }
    }
}
