package futures.future;

import futures.Waker;

final class Ready<T> implements Future<T> {
    private final T value;

    Ready(T value) {
        this.value = value;
    }

    @Override
    public T poll(Waker waker) {
        return this.value;
    }
}
