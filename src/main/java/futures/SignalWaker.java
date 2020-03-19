package futures;

import java.util.concurrent.atomic.AtomicBoolean;

public class SignalWaker implements Waker {
    private final Object signal = new Object();
    private final AtomicBoolean signalled = new AtomicBoolean();

    @Override
    public void wake() {
        synchronized (this.signal) {
            this.signalled.set(true);
            this.signal.notify();
        }
    }

    public void awaitSignal() throws InterruptedException {
        synchronized (this.signal) {
            if (this.signalled.getAndSet(false)) {
                return;
            }

            this.signal.wait();

            this.signalled.set(false);
        }
    }
}
