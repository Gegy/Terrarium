package dev.gegy.terrarium.backend.loader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class GuavaInMemoryCacher<K, V> implements Cacher<K, V> {
    private final Cache<K, CompletableFuture<Optional<V>>> cache;

    public GuavaInMemoryCacher(final Duration expiryTime, final int maximumSize) {
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(expiryTime)
                .maximumSize(maximumSize)
                .build();
    }

    @Override
    public CompletableFuture<Optional<V>> getOrLoad(final K key, final Supplier<CompletableFuture<Optional<V>>> loader) {
        try {
            return cache.get(key, loader::get);
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
