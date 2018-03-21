package net.gegy1000.earth.server.world.pipeline.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.zip.GZIPInputStream;

public class GoogleGeocoder implements Geocoder {
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final CoordinateState latLngCoordinateState;

    public GoogleGeocoder(CoordinateState coordinateState) {
        this.latLngCoordinateState = coordinateState;
    }

    @Override
    public Coordinate get(String place) throws IOException {
        String key = EarthRemoteData.info.getGeocoderKey();
        String request = "https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(place, "UTF-8") + "&key=" + key;

        URL url = new URL(request);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(8000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "terrarium-earth");
        connection.setRequestProperty("Accept-Encoding", "gzip");

        try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(new GZIPInputStream(connection.getInputStream())))) {
            JsonObject root = (JsonObject) JSON_PARSER.parse(input);

            if (root.has("status")) {
                String status = root.get("status").getAsString();
                if (status.equalsIgnoreCase("OVER_QUERY_LIMIT")) {
                    throw new IOException("Reached query limit for Google Geocoder API! Try again in a few minutes");
                } else if (status.equalsIgnoreCase("REQUEST_DENIED")) {
                    throw new IOException(root.get("error_message").getAsString());
                }
            }

            if (root.has("results")) {
                JsonArray results = root.getAsJsonArray("results");
                for (JsonElement element : results) {
                    JsonObject result = element.getAsJsonObject();
                    if (result.has("geometry")) {
                        JsonObject geometry = result.getAsJsonObject("geometry");
                        JsonObject location = geometry.getAsJsonObject("location");

                        return new Coordinate(this.latLngCoordinateState, location.get("lat").getAsDouble(), location.get("lng").getAsDouble());
                    }
                }
            }

            Terrarium.LOGGER.warn("Got geocoder response for {} with no result: {}", place, root);
        }

        return null;
    }
}
