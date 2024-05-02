package dev.gegy.terrarium.backend.earth.geocoder;

import dev.gegy.terrarium.backend.earth.GeoCoords;
import dev.gegy.terrarium.backend.loader.ConcurrencyLimiter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Geocoder {
    CompletableFuture<Optional<GeoCoords>> lookup(String query);

    CompletableFuture<List<String>> listSuggestions(String query);

    default Geocoder cached() {
        return new CachingGeocoder(this);
    }

    default Geocoder limitConcurrency(final ConcurrencyLimiter limiter) {
        return new Geocoder() {
            @Override
            public CompletableFuture<Optional<GeoCoords>> lookup(final String query) {
                return limiter.submit(() -> Geocoder.this.lookup(query));
            }

            @Override
            public CompletableFuture<List<String>> listSuggestions(final String query) {
                return limiter.submit(() -> Geocoder.this.listSuggestions(query));
            }
        };
    }

    final class LookupException extends RuntimeException {
        private final Error type;

        public LookupException(final Error type) {
            this.type = type;
        }

        public Error type() {
            return type;
        }
    }

    enum Error {
        RATE_LIMITED,
        UNKNOWN_ERROR,
    }
}
