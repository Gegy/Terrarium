package net.gegy1000.terrarium.server.util.tuple;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class Tuple5<A, B, C, D, E> {
    public final A a;
    public final B b;
    public final C c;
    public final D d;
    public final E e;

    public Tuple5(A a, B b, C c, D d, E e) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }

    public static <A, B, C, D, E> Optional<Tuple5<A, B, C, D, E>> join(
            Optional<A> a, Optional<B> b, Optional<C> c, Optional<D> d, Optional<E> e
    ) {
        if (a.isPresent() && b.isPresent() && c.isPresent() && d.isPresent() && e.isPresent()) {
            return Optional.of(new Tuple5<>(a.get(), b.get(), c.get(), d.get(), e.get()));
        }
        return Optional.empty();
    }

    public static <A, B, C, D, E> CompletableFuture<Tuple5<A, B, C, D, E>> join(
            CompletableFuture<A> a,
            CompletableFuture<B> b,
            CompletableFuture<C> c,
            CompletableFuture<D> d,
            CompletableFuture<E> e
    ) {
        return CompletableFuture.allOf(a, b, c, d, e)
                .thenApply(v -> new Tuple5<>(a.join(), b.join(), c.join(), d.join(), e.join()));
    }
}
