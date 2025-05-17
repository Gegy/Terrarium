package dev.gegy.terrarium.backend.earth;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.loader.HttpLoader;
import dev.gegy.terrarium.backend.util.Util;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record ApiKeys(
        Optional<String> geocoder,
        Optional<String> suggestions,
        Optional<String> streetView
) {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ApiKeys EMPTY = new ApiKeys(Optional.empty(), Optional.empty(), Optional.empty());

    public static final Codec<ApiKeys> CODEC = RecordCodecBuilder.create(i -> i.group(
            keyCodec(31).optionalFieldOf("geocoder_key").forGetter(ApiKeys::geocoder),
            keyCodec(961).optionalFieldOf("autocomplete_key").forGetter(ApiKeys::suggestions),
            keyCodec(729).optionalFieldOf("streetview_key").forGetter(ApiKeys::streetView)
    ).apply(i, ApiKeys::new));

    private static Codec<String> keyCodec(final int magic) {
        return Codec.STRING.xmap(
                encoded -> {
                    final byte[] encodedBytes = Base64.getDecoder().decode(encoded);
                    final byte[] decodedBytes = new byte[encodedBytes.length];
                    for (int i = 0; i < encodedBytes.length; i++) {
                        decodedBytes[i] = (byte) (encodedBytes[i] - (i << i) - magic);
                    }
                    return new String(decodedBytes, StandardCharsets.US_ASCII);
                },
                string -> string
        );
    }

    public static CompletableFuture<ApiKeys> fetch(final HttpClient httpClient) {
        final HttpRequest request = HttpRequest.newBuilder(URI.create("https://terrarium.gegy.dev/geo3/keys.json"))
                .header("User-Agent", HttpLoader.USER_AGENT)
                .timeout(Duration.ofMinutes(1))
                .GET()
                .build();
        return httpClient.sendAsync(request, Util.jsonBodyHandler(CODEC)).handle((response, throwable) -> {
            if (throwable != null) {
                LOGGER.error("Failed to fetch Terrarium API keys", throwable);
                return EMPTY;
            }
            return response.body();
        });
    }
}
