package net.gegy1000.terrarium.server.util.tuple;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class Tuple3<A, B, C> {
    public final A a;
    public final B b;
    public final C c;

    public Tuple3(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static <A, B, C> Optional<Tuple3<A, B, C>> join(Optional<A> a, Optional<B> b, Optional<C> c) {
        if (a.isPresent() && b.isPresent() && c.isPresent()) {
            return Optional.of(new Tuple3<>(a.get(), b.get(), c.get()));
        }
        return Optional.empty();
    }

    public static <A, B, C> CompletableFuture<Tuple3<A, B, C>> join(CompletableFuture<A> a, CompletableFuture<B> b, CompletableFuture<C> c) {
        return CompletableFuture.allOf(a, b, c).thenApply(v -> new Tuple3<>(a.join(), b.join(), c.join()));
    }
}
