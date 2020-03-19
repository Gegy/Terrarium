package futures.future;

import futures.Waker;

final class Pending<T> implements Future<T> {
    @Override
    public T poll(Waker waker) {
        return null;
    }
}
