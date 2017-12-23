package net.gegy1000.terrarium.server.map.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

public class GeocodingSource implements DataSource {
    private static final JsonParser JSON_PARSER = new JsonParser();

    private final EarthGenerationSettings settings;

    public GeocodingSource(EarthGenerationSettings settings) {
        this.settings = settings;
    }

    public Coordinate get(String place) throws IOException {
        String request = "https://maps.googleapis.com/maps/api/geocode/json?address=" + URLEncoder.encode(place, "UTF-8");

        URL url = new URL(request);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", Terrarium.MODID);

        try (InputStreamReader input = new InputStreamReader(connection.getInputStream())) {
            JsonObject root = (JsonObject) JSON_PARSER.parse(input);
            if (root.has("results")) {
                JsonArray results = root.getAsJsonArray("results");
                for (JsonElement element : results) {
                    JsonObject result = element.getAsJsonObject();
                    if (result.has("geometry")) {
                        JsonObject geometry = result.getAsJsonObject("geometry");
                        JsonObject location = geometry.getAsJsonObject("location");

                        return Coordinate.fromLatLng(this.settings, location.get("lat").getAsDouble(), location.get("lng").getAsDouble());
                    }
                }
            }
        }

        return null;
    }

    @Override
    public EarthGenerationSettings getSettings() {
        return this.settings;
    }
}
