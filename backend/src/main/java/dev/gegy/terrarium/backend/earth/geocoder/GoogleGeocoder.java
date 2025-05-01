package dev.gegy.terrarium.backend.earth.geocoder;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.earth.ApiKeys;
import dev.gegy.terrarium.backend.earth.GeoCoords;
import dev.gegy.terrarium.backend.loader.HttpLoader;
import dev.gegy.terrarium.backend.util.Util;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GoogleGeocoder implements Geocoder {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Duration LOOKUP_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration SUGGEST_TIMEOUT = Duration.ofSeconds(5);

    private static final URI SUGGEST_URI = URI.create("https://places.googleapis.com/v1/places:autocomplete");

    private final HttpClient httpClient;
    private final ApiKeys apiKeys;

    public GoogleGeocoder(final HttpClient httpClient, final ApiKeys apiKeys) {
        this.httpClient = httpClient;
        this.apiKeys = apiKeys;
    }

    @Override
    public CompletableFuture<Optional<GeoCoords>> lookup(final String query) {
        if (query.isEmpty()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        if (apiKeys.geocoder().isEmpty()) {
            return CompletableFuture.failedFuture(new LookupException(Geocoder.Error.UNKNOWN_ERROR));
        }

        final String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        final URI uri = URI.create("https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedQuery + "&key=" + apiKeys.geocoder().get());
        final HttpRequest request = HttpRequest.newBuilder(uri)
                .header("User-Agent", HttpLoader.USER_AGENT)
                .timeout(LOOKUP_TIMEOUT)
                .GET()
                .build();

        return httpClient.sendAsync(request, Util.jsonBodyHandler(GeocodeResult.CODEC))
                .exceptionally(throwable -> {
                    LOGGER.error("Failed to parse geocoder response for query: '{}'", query, throwable);
                    throw new LookupException(Geocoder.Error.UNKNOWN_ERROR);
                })
                .thenApply(response -> switch (response.body().status()) {
                    case OK, ZERO_RESULTS -> {
                        final List<GeoCoords> results = response.body().entries();
                        yield !results.isEmpty() ? Optional.of(results.getFirst()) : Optional.empty();
                    }
                    case OVER_QUERY_LIMIT, OVER_DAILY_LIMIT, REQUEST_DENIED -> throw new LookupException(Geocoder.Error.RATE_LIMITED);
                    case UNKNOWN_ERROR, INVALID_REQUEST -> throw new LookupException(Geocoder.Error.UNKNOWN_ERROR);
                });
    }

    @Override
    public CompletableFuture<List<String>> listSuggestions(final String query) {
        if (query.isBlank()) {
            return CompletableFuture.completedFuture(List.of());
        }

        if (apiKeys.suggestions().isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        final JsonObject requestBody = new JsonObject();
        requestBody.addProperty("input", query);

        final HttpRequest request = HttpRequest.newBuilder(SUGGEST_URI)
                .header("User-Agent", HttpLoader.USER_AGENT)
                .header("X-Goog-API-Key", apiKeys.suggestions().get())
                .timeout(SUGGEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return httpClient.sendAsync(request, Util.jsonBodyHandler(SuggestResult.FALLIBLE_CODEC))
                .<List<String>>thenApply(response -> response.body().map(
                        result -> result.entries().stream()
                                .flatMap(entry -> entry.name.stream())
                                .toList(),
                        error -> {
                            LOGGER.error("Failed to get suggestions for query '{}': {}", query, error);
                            return List.of();
                        }
                ))
                .exceptionally(throwable -> {
                    LOGGER.error("Failed to parse suggestion response for query: '{}'", query, throwable);
                    return List.of();
                });
    }

    private record GeocodeResult(Status status, List<GeoCoords> entries) {
        private static final Codec<GeoCoords> ENTRY_CODEC = GeoCoords.GOOGLE_CODEC.fieldOf("location").fieldOf("geometry").codec();

        public static final Codec<GeocodeResult> CODEC = RecordCodecBuilder.create(i -> i.group(
                Status.CODEC.fieldOf("status").forGetter(GeocodeResult::status),
                ENTRY_CODEC.listOf().optionalFieldOf("results", List.of()).forGetter(GeocodeResult::entries)
        ).apply(i, GeocodeResult::new));
    }

    private enum Status {
        OK("OK"),
        ZERO_RESULTS("ZERO_RESULTS"),
        OVER_DAILY_LIMIT("OVER_DAILY_LIMIT"),
        OVER_QUERY_LIMIT("OVER_QUERY_LIMIT"),
        REQUEST_DENIED("REQUEST_DENIED"),
        INVALID_REQUEST("INVALID_REQUEST"),
        UNKNOWN_ERROR("UNKNOWN_ERROR"),
        ;

        public static final Codec<Status> CODEC = Util.stringLookupCodec(values(), s -> s.id);

        private final String id;

        Status(final String id) {
            this.id = id;
        }
    }

    private record SuggestResult(List<Entry> entries) {
        public static final Codec<SuggestResult> CODEC = RecordCodecBuilder.create(i -> i.group(
                Entry.CODEC.listOf().optionalFieldOf("suggestions", List.of()).forGetter(SuggestResult::entries)
        ).apply(i, SuggestResult::new));

        public static final Codec<Either<SuggestResult, Error>> FALLIBLE_CODEC = Codec.either(CODEC, Error.CODEC.fieldOf("error").codec());

        private record Entry(Optional<String> name) {
            private static final Codec<String> TEXT_CODEC = Codec.STRING.fieldOf("text").fieldOf("text").codec();

            public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                    TEXT_CODEC.optionalFieldOf("placePrediction").forGetter(Entry::name)
            ).apply(i, Entry::new));
        }
    }

    private record Error(int code, String message, Status status) {
        public static final Codec<Error> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("code").forGetter(Error::code),
                Codec.STRING.fieldOf("message").forGetter(Error::message),
                Status.CODEC.fieldOf("status").forGetter(Error::status)
        ).apply(i, Error::new));
    }
}
