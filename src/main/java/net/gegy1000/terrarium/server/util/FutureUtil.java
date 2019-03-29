package net.gegy1000.terrarium.server.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class FutureUtil {
    public static <T> CompletableFuture<Collection<T>> joinAll(Collection<CompletableFuture<T>> collection) {
        CompletableFuture[] array = collection.toArray(new CompletableFuture[0]);
        return CompletableFuture.allOf(array)
                .thenApply(v -> collection.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    public static <T> CompletableFuture<Collection<T>> joinAll(CompletableFuture<T>... array) {
        return CompletableFuture.allOf(array)
                .thenApply(v -> Arrays.stream(array)
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }
}
