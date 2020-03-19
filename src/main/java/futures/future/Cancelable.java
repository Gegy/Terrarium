package futures.future;

import futures.Waker;

import java.util.concurrent.CancellationException;

public final class Cancelable<T> implements Future<T> {
    private final Future<T> inner;
    private boolean canceled;

    private Waker waker;

    Cancelable(Future<T> inner) {
        this.inner = inner;
    }

    @Override
    public synchronized T poll(Waker waker) {
        if (this.canceled) {
            throw new CancellationException("task canceled");
        }
        this.waker = waker;
        return this.inner.poll(waker);
    }

    public synchronized void cancel() {
        this.canceled = true;
        if (this.waker != null) {
            this.waker.wake();
        }
    }
}
