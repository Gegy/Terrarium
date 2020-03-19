package futures.executor;

import futures.Waker;
import futures.future.Future;

import java.util.concurrent.LinkedBlockingDeque;

public final class LocalExecutor {
    private final LinkedBlockingDeque<Task<?>> taskQueue = new LinkedBlockingDeque<>();

    public <T> TaskHandle<T> spawn(Future<T> future) {
        Task<T> task = new Task<>(future, EnqueuingWaker::new);
        this.taskQueue.add(task);
        return task.handle;
    }

    public <T> Future<T> steal(TaskHandle<T> handle) {
        if (!this.taskQueue.remove(handle.task)) {
            System.out.println("failed to steal"); // FIXME: could be in-between advancing and waking (is this possible)
        }
        return handle.steal();
    }

    public boolean remove(TaskHandle<?> handle) {
        return this.taskQueue.remove(handle.task);
    }

    public void advanceAll() {
        while (!this.taskQueue.isEmpty()) {
            Task<?> task = this.taskQueue.remove();
            ((EnqueuingWaker) task.waker).reset();
            task.advance();
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
            if (!this.awoken) {
                LocalExecutor.this.taskQueue.add(this.task);
                this.awoken = true;
            }
        }
    }
}
