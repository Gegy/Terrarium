package net.gegy1000.terrarium.server.util;

import net.gegy1000.terrarium.server.util.tuple.Tuple2;
import net.gegy1000.terrarium.server.util.tuple.Tuple3;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FutureUtil {
    public static <T> CompletableFuture<Collection<T>> allOf(Collection<CompletableFuture<T>> collection) {
        CompletableFuture[] array = collection.toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(array)
                .thenApply(v -> collection.stream()
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

    public static <A, B> CompletableFuture<Tuple2<A, B>> join2(CompletableFuture<A> a, CompletableFuture<B> b) {
        return CompletableFuture.allOf(a, b).thenApply(v -> new Tuple2<>(a.join(), b.join()));
    }

    public static <A, B, C> CompletableFuture<Tuple3<A, B, C>> join3(CompletableFuture<A> a, CompletableFuture<B> b, CompletableFuture<C> c) {
        return CompletableFuture.allOf(a, b, c).thenApply(v -> new Tuple3<>(a.join(), b.join(), c.join()));
    }
}
