package net.gegy1000.earth.server.world.pipeline.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;

import javax.vecmath.Vector2d;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NominatimGeocoder implements Geocoder {
    private static final String GEOCODER_ADDRESS = "https://nominatim.openstreetmap.org/search/%s?format=jsonv2&limit=4";

    private static final JsonParser JSON_PARSER = new JsonParser();

    @Override
    public Vector2d get(String place) throws IOException {
        String encodedPlace = URLEncoder.encode(place, "UTF-8").replace("+", "%20");
        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(GEOCODER_ADDRESS, encodedPlace)).openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("User-Agent", "terrarium-earth");
        connection.setRequestProperty("Referer", "https://github.com/gegy1000/Terrarium");

        connection.setConnectTimeout(4000);
        connection.setReadTimeout(30000);

        try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(connection.getInputStream()))) {
            JsonArray root = (JsonArray) JSON_PARSER.parse(input);

            for (JsonElement element : root) {
                JsonObject resultRoot = element.getAsJsonObject();
                if (resultRoot.has("lat") && resultRoot.has("lon")) {
                    try {
                        double latitude = Double.parseDouble(resultRoot.get("lat").getAsString());
                        double longitude = Double.parseDouble(resultRoot.get("lon").getAsString());
                        return new Vector2d(latitude, longitude);
                    } catch (NumberFormatException e) {
                        Terrarium.LOGGER.error("Received malformed Nominatim latitude/longitude", e);
                    }
                }
            }

            Terrarium.LOGGER.warn("Got geocoder response for {} with no result: {}", place, root);
        }

        return null;
    }

    @Override
    public String[] suggest(String place) {
        return null;
    }
}
