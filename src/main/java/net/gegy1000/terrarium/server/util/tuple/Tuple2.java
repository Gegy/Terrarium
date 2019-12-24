package net.gegy1000.terrarium.server.util.tuple;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class Tuple2<A, B> {
    public final A a;
    public final B b;

    public Tuple2(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> Optional<Tuple2<A, B>> join(Optional<A> a, Optional<B> b) {
        if (a.isPresent() && b.isPresent()) {
            return Optional.of(new Tuple2<>(a.get(), b.get()));
        }
        return Optional.empty();
    }

    public static <A, B> CompletableFuture<Tuple2<A, B>> join(CompletableFuture<A> a, CompletableFuture<B> b) {
        return CompletableFuture.allOf(a, b).thenApply(v -> new Tuple2<>(a.join(), b.join()));
    }
}
