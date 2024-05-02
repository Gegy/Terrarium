package dev.gegy.terrarium.backend.earth.geocoder;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.gegy.terrarium.backend.earth.GeoCoords;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CachingGeocoder implements Geocoder {
    private static final Duration EXPIRY_TIME = Duration.ofMinutes(30);

    private final Geocoder delegate;

    private final Cache<String, Optional<GeoCoords>> lookupCache = CacheBuilder.newBuilder()
            .expireAfterAccess(EXPIRY_TIME)
            .maximumSize(50)
            .build();
    private final Cache<String, List<String>> suggestionCache = CacheBuilder.newBuilder()
            .expireAfterAccess(EXPIRY_TIME)
            .maximumSize(200)
            .build();

    public CachingGeocoder(final Geocoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<Optional<GeoCoords>> lookup(final String query) {
        final String key = queryKey(query);
        final Optional<GeoCoords> cachedResult = lookupCache.getIfPresent(key);
        if (cachedResult != null) {
            return CompletableFuture.completedFuture(cachedResult);
        }
        final CompletableFuture<Optional<GeoCoords>> future = delegate.lookup(query);
        future.thenAccept(result -> lookupCache.put(key, result));
        return future;
    }

    @Override
    public CompletableFuture<List<String>> listSuggestions(final String query) {
        final String key = queryKey(query);
        final List<String> cachedSuggestions = suggestionCache.getIfPresent(key);
        if (cachedSuggestions != null) {
            return CompletableFuture.completedFuture(cachedSuggestions);
        }
        final CompletableFuture<List<String>> future = delegate.listSuggestions(query);
        future.thenAccept(suggestions -> suggestionCache.put(key, suggestions));
        return future;
    }

    private static String queryKey(final String query) {
        return query.trim().toLowerCase(Locale.ROOT);
    }
}
