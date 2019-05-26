package net.gegy1000.earth.server.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class ProgressTracker implements AutoCloseable {
    private final ProcessTracker process;

    private final String description;
    private final int totalSteps;
    private final AtomicInteger steps = new AtomicInteger();

    boolean errored;

    ProgressTracker(ProcessTracker process, String description, int totalSteps) {
        this.process = process;
        this.description = description;
        this.totalSteps = totalSteps;
    }

    public void use(Use use) {
        try {
            use.use();
        } catch (Exception e) {
            this.process.raiseException(e);
        } finally {
            this.close();
        }
    }

    public String getDescription() {
        return this.description;
    }

    public double getProgress() {
        return (double) this.steps.get() / this.totalSteps;
    }

    public boolean isComplete() {
        return this.steps.get() >= this.totalSteps;
    }

    public boolean isErrored() {
        return this.errored;
    }

    public void step(int steps) {
        if (this.steps.addAndGet(steps) >= this.totalSteps) {
            this.markComplete();
        }
    }

    public void markComplete() {
        this.steps.set(this.totalSteps);
    }

    @Override
    public void close() {
        this.markComplete();
        this.process.pop(this);
    }

    public interface Use {
        void use() throws Exception;
    }
}
