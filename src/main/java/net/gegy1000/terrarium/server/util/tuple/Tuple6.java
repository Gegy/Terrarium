package net.gegy1000.terrarium.server.util.tuple;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class Tuple6<A, B, C, D, E, F> {
    public final A a;
    public final B b;
    public final C c;
    public final D d;
    public final E e;
    public final F f;

    public Tuple6(A a, B b, C c, D d, E e, F f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f =f;
    }

    public static <A, B, C, D, E, F> Optional<Tuple6<A, B, C, D, E, F>> join(
            Optional<A> a, Optional<B> b, Optional<C> c, Optional<D> d, Optional<E> e, Optional<F> f
    ) {
        if (a.isPresent() && b.isPresent() && c.isPresent() && d.isPresent() && e.isPresent() && f.isPresent()) {
            return Optional.of(new Tuple6<>(a.get(), b.get(), c.get(), d.get(), e.get(), f.get()));
        }
        return Optional.empty();
    }

    public static <A, B, C, D, E, F> CompletableFuture<Tuple6<A, B, C, D, E, F>> join(
            CompletableFuture<A> a,
            CompletableFuture<B> b,
            CompletableFuture<C> c,
            CompletableFuture<D> d,
            CompletableFuture<E> e,
            CompletableFuture<F> f
    ) {
        return CompletableFuture.allOf(a, b, c, d, e, f)
                .thenApply(v -> new Tuple6<>(a.join(), b.join(), c.join(), d.join(), e.join(), f.join()));
    }
}
