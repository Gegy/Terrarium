package net.gegy1000.terrarium.server.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FutureUtil {
    public static <T> CompletableFuture<Collection<T>> allOf(Collection<CompletableFuture<T>> collection) {
        CompletableFuture[] array = collection.toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(array)
                .thenApply(v -> collection.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    public static <K, V> CompletableFuture<Map<K, V>> allOf(Map<K, CompletableFuture<V>> map) {
        CompletableFuture[] array = map.values().toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(array)
                .thenApply(v -> map.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                value -> value.getValue().join()
                        ))
                );
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<Collection<T>> allOf(Stream<CompletableFuture<T>> collection) {
        CompletableFuture<T>[] array = collection.toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(array)
                .thenApply(v -> Arrays.stream(array)
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    @SafeVarargs
    public static <T> CompletableFuture<Collection<T>> allOf(CompletableFuture<T>... array) {
        return CompletableFuture.allOf(array)
                .thenApply(v -> Arrays.stream(array)
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    public static void joinAll(Collection<? extends CompletableFuture<?>> futures) {
        CompletableFuture[] array = futures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(array).join();
    }

    public static <A, B, R> CompletableFuture<R> map2(CompletableFuture<A> a, CompletableFuture<B> b, Map2<A, B, R> map) {
        return CompletableFuture.allOf(a, b).thenApply(v -> map.apply(a.join(), b.join()));
    }

    public static <A, B, C, R> CompletableFuture<R> map3(CompletableFuture<A> a, CompletableFuture<B> b, CompletableFuture<C> c, Map3<A, B, C, R> map) {
        return CompletableFuture.allOf(a, b, c).thenApply(v -> map.apply(a.join(), b.join(), c.join()));
    }

    public interface Map2<A, B, R> {
        R apply(A a, B b);
    }

    public interface Map3<A, B, C, R> {
        R apply(A a, B b, C c);
    }
}
