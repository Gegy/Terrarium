package futures.future;

import futures.Waker;

import java.util.function.BiFunction;

final class Map2<A, B, R> implements Future<R> {
    private final Future<A> futureA;
    private final Future<B> futureB;
    private final BiFunction<A, B, R> map;

    private A a;
    private B b;

    Map2(Future<A> a, Future<B> b, BiFunction<A, B, R> map) {
        this.futureA = a;
        this.futureB = b;
        this.map = map;
    }

    @Override
    public R poll(Waker waker) {
        if (this.a == null) {
            this.a = this.futureA.poll(waker);
        }

        if (this.b == null) {
            this.b = this.futureB.poll(waker);
        }

        if (this.a != null && this.b != null) {
            return this.map.apply(this.a, this.b);
        }

        return null;
    }
}
