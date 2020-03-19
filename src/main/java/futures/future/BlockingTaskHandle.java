package futures.future;

import futures.Waker;

import java.util.concurrent.CancellationException;

public final class BlockingTaskHandle<T> implements Future<T> {
    private T result;
    private boolean canceled;

    private Thread executingThread;
    private Waker waker;

    BlockingTaskHandle() {
    }

    @Override
    public synchronized T poll(Waker waker) {
        if (this.canceled) {
            throw new CancellationException("task canceled");
        }
        if (this.result != null) {
            return this.result;
        }
        this.waker = waker;
        return null;
    }

    synchronized void setExecutingThread(Thread thread) {
        this.executingThread = thread;
    }

    synchronized void complete(T result) {
        this.result = result;
        if (this.waker != null) {
            this.waker.wake();
        }
    }

    public synchronized void cancel() {
        this.canceled = true;
        if (this.waker != null) {
            this.waker.wake();
        }
        if (this.executingThread != null) {
            this.executingThread.interrupt();
        }
    }
}
