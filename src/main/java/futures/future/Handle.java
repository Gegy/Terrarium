package futures.future;

import futures.Waker;

import java.util.function.BiFunction;

final class Handle<T, U> implements Future<U> {
    private final Future<T> future;
    private final BiFunction<T, Throwable, U> handle;

    Handle(Future<T> future, BiFunction<T, Throwable, U> handle) {
        this.future = future;
        this.handle = handle;
    }

    @Override
    public U poll(Waker waker) {
        try {
            T result = this.future.poll(waker);
            if (result != null) {
                return this.handle.apply(result, null);
            }
            return null;
        } catch (Throwable t) {
            return this.handle.apply(null, t);
        }
    }
}
