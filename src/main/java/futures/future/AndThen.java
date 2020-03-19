package futures.future;

import futures.Waker;

import java.util.function.Function;

final class AndThen<T, U> implements Future<U> {
    private final Future<T> first;
    private final Function<T, Future<U>> andThen;
    private Future<U> thenFuture;

    AndThen(Future<T> first, Function<T, Future<U>> andThen) {
        this.first = first;
        this.andThen = andThen;
    }

    @Override
    public U poll(Waker waker) {
        if (this.thenFuture == null) {
            T poll = this.first.poll(waker);
            if (poll != null) {
                this.thenFuture = this.andThen.apply(poll);
            }
        }

        if (this.thenFuture != null) {
            return this.thenFuture.poll(waker);
        }

        return null;
    }
}
