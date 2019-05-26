package net.gegy1000.earth.server.util;

import javax.annotation.Nullable;
import java.util.LinkedList;

public final class ProcessTracker {
    private final LinkedList<ProgressTracker> trackers = new LinkedList<>();

    private boolean complete;
    private Exception error;

    public synchronized ProgressTracker push(String description, int totalSteps) {
        ProgressTracker tracker = new ProgressTracker(this, description, totalSteps);
        if (this.isFrozen()) {
            return tracker;
        }
        this.trackers.addLast(tracker);
        return tracker;
    }

    public synchronized void pop(ProgressTracker tracker) {
        if (this.isFrozen()) {
            return;
        }
        ProgressTracker popped = this.trackers.removeLast();
        if (tracker != popped) {
            throw new IllegalArgumentException("Can only pop from top of stack!");
        }
    }

    public void raiseException(Exception exception) {
        ProgressTracker errored = this.trackers.peekLast();
        if (errored == null) {
            throw new IllegalStateException("Cannot raise exception with no trackers");
        }

        errored.errored = true;
        this.error = exception;
    }

    public void markComplete() {
        this.complete = true;
    }

    public boolean isComplete() {
        return this.complete;
    }

    public boolean isErrored() {
        return this.error != null;
    }

    @Nullable
    public Exception getException() {
        return this.error;
    }

    private boolean isFrozen() {
        return this.complete || this.error != null;
    }

    public synchronized void forEach(ForEach handler) {
        int index = 0;
        for (ProgressTracker tracker : this.trackers) {
            handler.accept(tracker, index++);
        }
    }

    public interface ForEach {
        void accept(ProgressTracker tracker, int index);
    }
}
