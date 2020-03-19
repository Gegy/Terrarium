package futures.executor;

import futures.Waker;
import futures.future.Future;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;

public final class ThreadedExecutor implements AutoCloseable {
    private final Worker[] workers;
    private final LinkedBlockingDeque<Task<?>> taskQueue = new LinkedBlockingDeque<>();

    private boolean active = true;

    public ThreadedExecutor(int threadCount, ThreadFactory factory) {
        this.workers = new Worker[threadCount];
        for (int i = 0; i < threadCount; i++) {
            this.workers[i] = new Worker(factory);
        }
        for (Worker worker : this.workers) {
            worker.start();
        }
    }

    public <T> TaskHandle<T> spawn(Future<T> future) {
        Task<T> task = new Task<>(future, EnqueuingWaker::new);
        this.taskQueue.add(task);
        return task.handle;
    }

    public <T> Future<T> steal(TaskHandle<T> handle) {
        if (!ThreadedExecutor.this.taskQueue.remove(handle.task)) {
            System.out.println("failed to steal"); // FIXME: could be in-between advancing and waking (is this possible)
        }
        return handle.steal();
    }

    @Override
    public void close() {
        this.active = false;
        this.taskQueue.clear();
        for (Worker worker : this.workers) {
            worker.thread.interrupt();
        }
    }

    private class Worker {
        private final Thread thread;

        Worker(ThreadFactory factory) {
            this.thread = factory.newThread(this::drive);
        }

        public void start() {
            this.thread.start();
        }

        private void drive() {
            try {
                while (ThreadedExecutor.this.active) {
                    Task<?> task = ThreadedExecutor.this.taskQueue.take();
                    ((EnqueuingWaker) task.waker).reset();
                    task.advance();
                }
            } catch (InterruptedException e) {
                // interrupted by executor
            }
        }
    }

    private class EnqueuingWaker implements Waker {
        private final Task<?> task;
        boolean awoken;

        private EnqueuingWaker(Task<?> task) {
            this.task = task;
        }

        void reset() {
            this.awoken = false;
        }

        @Override
        public void wake() {
            ThreadedExecutor.this.taskQueue.add(this.task);
        }
    }
}
