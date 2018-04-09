package net.gegy1000.earth.server.world.pipeline.source;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.coordinate.CoordinateState;
import net.gegy1000.terrarium.server.world.pipeline.source.Geocoder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class GoogleGeocoder implements Geocoder {
    private static final String GEOCODER_ADDRESS = "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s";
    private static final String SUGGESTION_ADDRESS = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%s&types=geocode&key=%s";

    private static final JsonParser JSON_PARSER = new JsonParser();

    private final CloseableHttpClient client = HttpClientBuilder.create()
            .addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                request.setHeader("Accent-Encoding", "gzip");
                request.setHeader("User-Agent", "terrarium-earth");
                request.setHeader("Referer", "https://github.com/gegy1000/Terrarium");
            })
            .addInterceptorFirst((HttpResponseInterceptor) (response, context) -> {
                HttpEntity entity = response.getEntity();
                Arrays.stream(entity.getContentEncoding().getElements())
                        .filter(element -> element.getName().equalsIgnoreCase("gzip"))
                        .findFirst()
                        .ifPresent(element -> response.setEntity(new GzipDecompressingEntity(entity)));
            })
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(2000)
                    .setConnectionRequestTimeout(2000)
                    .setSocketTimeout(30000)
                    .build())
            .build();

    private final CoordinateState latLngCoordinateState;

    public GoogleGeocoder(CoordinateState coordinateState) {
        this.latLngCoordinateState = coordinateState;
    }

    @Override
    public Coordinate get(String place) throws IOException {
        String key = EarthRemoteData.info.getGeocoderKey();
        HttpGet request = new HttpGet(String.format(GEOCODER_ADDRESS, URLEncoder.encode(place, "UTF-8"), key));

        CloseableHttpResponse response = this.client.execute(request);

        HttpEntity entity = response.getEntity();
        try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(entity.getContent()))) {
            JsonObject root = (JsonObject) JSON_PARSER.parse(input);

            this.handleResponseStatus(root);

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

    @Override
    public List<String> suggestCommand(String place) {
        return Collections.emptyList();
    }

    @Override
    public String[] suggest(String place) throws IOException {
        String key = EarthRemoteData.info.getAutocompleteKey();
        HttpGet request = new HttpGet(String.format(SUGGESTION_ADDRESS, URLEncoder.encode(place, "UTF-8"), key));

        CloseableHttpResponse response = this.client.execute(request);

        HttpEntity entity = response.getEntity();
        try (InputStreamReader input = new InputStreamReader(new BufferedInputStream(entity.getContent()))) {
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
