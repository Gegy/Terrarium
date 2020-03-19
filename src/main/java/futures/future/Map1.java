package futures.future;

import futures.Waker;

import java.util.function.Function;

final class Map1<T, U> implements Future<U> {
    private final Future<T> future;
    private final Function<T, U> map;

    Map1(Future<T> a, Function<T, U> map) {
        this.future = a;
        this.map = map;
    }

    @Override
    public U poll(Waker waker) {
        T poll = this.future.poll(waker);
        if (poll != null) {
            return this.map.apply(poll);
        }
        return null;
    }
}
