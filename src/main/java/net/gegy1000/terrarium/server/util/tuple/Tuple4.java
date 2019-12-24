package net.gegy1000.terrarium.server.util.tuple;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class Tuple4<A, B, C, D> {
    public final A a;
    public final B b;
    public final C c;
    public final D d;

    public Tuple4(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    public static <A, B, C, D> Optional<Tuple4<A, B, C, D>> join(
            Optional<A> a, Optional<B> b, Optional<C> c, Optional<D> d
    ) {
        if (a.isPresent() && b.isPresent() && c.isPresent() && d.isPresent()) {
            return Optional.of(new Tuple4<>(a.get(), b.get(), c.get(), d.get()));
        }
        return Optional.empty();
    }

    public static <A, B, C, D> CompletableFuture<Tuple4<A, B, C, D>> join(
            CompletableFuture<A> a, CompletableFuture<B> b, CompletableFuture<C> c, CompletableFuture<D> d
    ) {
        return CompletableFuture.allOf(a, b, c, d).thenApply(v -> new Tuple4<>(a.join(), b.join(), c.join(), d.join()));
    }
}
