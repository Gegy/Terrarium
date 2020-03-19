package futures.executor;

import futures.Waker;
import futures.future.Future;

import java.util.function.Function;

final class Task<T> {
    final Future<T> future;
    final Waker waker;

    boolean complete;
    TaskHandle<T> handle;

    Task(Future<T> future, Function<Task<T>, Waker> waker) {
        this.future = future;
        this.handle = new TaskHandle<>(this);
        this.waker = waker.apply(this);
    }

    void advance() {
        if (this.complete) {
            throw new IllegalStateException("already complete");
        }

        T result = this.future.poll(this.waker);
        if (result != null) {
            this.complete = true;
            this.handle.complete(result);
        }
    }
}
