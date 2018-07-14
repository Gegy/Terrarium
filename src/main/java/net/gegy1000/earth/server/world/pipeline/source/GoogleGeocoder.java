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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GoogleGeocoder implements Geocoder {
    private static final String GEOCODER_ADDRESS = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";
    private static final String SUGGESTION_ADDRESS = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%s&types=geocode&key=%s";

    private static final JsonParser JSON_PARSER = new JsonParser();

    @Override
    public Vector2d get(String place) throws IOException {
        String key = EarthRemoteData.info.getGeocoderKey();

        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(GEOCODER_ADDRESS, URLEncoder.encode(place, "UTF-8"), key)).openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("User-Agent", "terrarium-earth");
        connection.setRequestProperty("Referer", "https://github.com/gegy1000/Terrarium");

        connection.setConnectTimeout(4000);
        connection.setReadTimeout(30000);

        try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(connection.getInputStream()))) {
            JsonObject root = (JsonObject) JSON_PARSER.parse(input);

            this.handleResponseStatus(root);

            if (root.has("results")) {
                JsonArray results = root.getAsJsonArray("results");
                for (JsonElement element : results) {
                    JsonObject result = element.getAsJsonObject();
                    if (result.has("geometry")) {
                        JsonObject geometry = result.getAsJsonObject("geometry");
                        JsonObject location = geometry.getAsJsonObject("location");

                        return new Vector2d(location.get("lat").getAsDouble(), location.get("lng").getAsDouble());
                    }
                }
            }

            Terrarium.LOGGER.warn("Got geocoder response for {} with no result: {}", place, root);
        }

        return null;
    }

    @Override
    public String[] suggest(String place) throws IOException {
        String key = EarthRemoteData.info.getAutocompleteKey();

        HttpURLConnection connection = (HttpURLConnection) new URL(String.format(SUGGESTION_ADDRESS, URLEncoder.encode(place, "UTF-8"), key)).openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("User-Agent", "terrarium-earth");
        connection.setRequestProperty("Referer", "https://github.com/gegy1000/Terrarium");

        connection.setConnectTimeout(2000);
        connection.setReadTimeout(30000);

        try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(connection.getInputStream()))) {
            JsonObject root = (JsonObject) JSON_PARSER.parse(input);

            this.handleResponseStatus(root);

            if (root.has("predictions")) {
                List<String> predictions = new LinkedList<>();

                JsonArray predictionsArray = root.getAsJsonArray("predictions");
                for (JsonElement element : predictionsArray) {
                    JsonObject prediction = element.getAsJsonObject();
                    predictions.add(prediction.get("description").getAsString());
                }

                return predictions.toArray(new String[0]);
            }
        }

        return new String[0];
    }

    private void handleResponseStatus(JsonObject root) throws IOException {
        if (root.has("status")) {
            String status = root.get("status").getAsString();
            if (status.equalsIgnoreCase("OVER_QUERY_LIMIT")) {
                throw new IOException("Reached query limit for Google API! Try again in a few minutes");
            } else if (status.equalsIgnoreCase("REQUEST_DENIED")) {
                throw new IOException(root.get("error_message").getAsString());
            }
        }
    }
}
