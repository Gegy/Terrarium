package net.gegy1000.earth.client.render;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.earth.server.world.data.EarthRemoteData;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PanoramaLookupHandler {
    private static final String METADATA_ADDRESS = "https://maps.googleapis.com/maps/api/streetview/metadata?location=%.5f,%.5f&radius=150&source=outdoor&key=%s";
    private static final String PANORAMA_ADDRESS = "https://geo0.ggpht.com/cbk?cb_client=maps_sv.tactile&panoid=%s&output=tile&x=%s&y=%s&zoom=%s&nbt&fover=2";

    private static final String FAKE_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/61.0";

    private static final JsonParser JSON_PARSER = new JsonParser();

    @Nullable
    public static Result queryPanorama(double latitude, double longitude) throws IOException {
        String key = EarthRemoteData.info.getStreetviewKey();

        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(METADATA_ADDRESS, latitude, longitude, key)).openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("User-Agent", "terrarium-earth");
        connection.setRequestProperty("Referer", "https://github.com/gegy1000/Terrarium");

        connection.setConnectTimeout(4000);
        connection.setReadTimeout(30000);

        try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(connection.getInputStream()))) {
            JsonObject root = (JsonObject) JSON_PARSER.parse(input);

            PanoramaLookupHandler.handleResponseStatus(root);

            if (root.has("location")) {
                JsonObject location = root.getAsJsonObject("location");
                double panoLat = location.get("lat").getAsDouble();
                double panoLon = location.get("lng").getAsDouble();

                String panoId = root.get("pano_id").getAsString();

                return new Result(panoId, panoLat, panoLon);
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

    public static BufferedImage loadPanoramaTile(String panoramaId, int tileX, int tileY, int zoom) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(PANORAMA_ADDRESS, panoramaId, tileX, tileY, zoom)).openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        connection.setRequestProperty("Connection", "keep-alive");
        connection.setRequestProperty("Host", "geo0.ggpht.com");
        connection.setRequestProperty("Origin", "https://www.google.com");
        connection.setRequestProperty("Referer", "https://www.google.com/");
        connection.setRequestProperty("User-Agent", FAKE_USER_AGENT);

        connection.setConnectTimeout(4000);
        connection.setReadTimeout(30000);

        try (InputStream input = new BufferedInputStream(connection.getInputStream())) {
            return ImageIO.read(input);
        }
    }

    public static class Result {
        private final String id;
        private final double latitude;
        private final double longitude;

        public Result(String id, double latitude, double longitude) {
            this.id = id;
            this.latitude = latitude;
            this.longitude = longitude;
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
    }
}
