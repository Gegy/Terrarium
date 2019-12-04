package net.gegy1000.terrarium.server.world.data;

import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public final class DistributedColumnLoader implements ColumnDataLoader {
    private static final int WORKER_COUNT = 1;

    private final Function<ChunkPos, ColumnData> generator;

    private final Worker[] workers = new Worker[WORKER_COUNT];

    private final LinkedBlockingQueue<Work> workQueue = new LinkedBlockingQueue<>();
    private final Map<ChunkPos, Work> unseenWorkMap = new HashMap<>();
    private final Object unseenWorkMutex = new Object();

    private boolean active = true;

    public DistributedColumnLoader(Function<ChunkPos, ColumnData> generator) {
        this.generator = generator;

        for (int index = 0; index < WORKER_COUNT; index++) {
            Worker worker = new Worker(index);
            worker.start();

            this.workers[index] = worker;
        }
    }

    @Override
    public CompletableFuture<ColumnData> getAsync(ChunkPos columnPos) {
        CompletableFuture<ColumnData> future = new CompletableFuture<>();

        Work work = new Work(columnPos, future);
        this.workQueue.add(work);
        synchronized (this.unseenWorkMutex) {
            this.unseenWorkMap.put(columnPos, work);
        }

        return future;
    }

    @Override
    public ColumnData get(ChunkPos columnPos) {
        Work unseenWork;
        synchronized (this.unseenWorkMutex) {
            unseenWork = this.unseenWorkMap.remove(columnPos);
            if (unseenWork != null) {
                unseenWork.cancel();
            }
        }

        return this.generate(columnPos);
    }

    @Override
    public void close() {
        for (Worker worker : this.workers) {
            worker.interrupt();
        }
        this.active = false;
    }

    ColumnData generate(ChunkPos columnPos) {
        return this.generator.apply(columnPos);
    }

    void processWork(Consumer<Work> worker) throws InterruptedException {
        Work work;
        do {
            work = this.workQueue.take();
            synchronized (this.unseenWorkMutex) {
                this.unseenWorkMap.remove(work.columnPos);
            }
        } while (work.isCancelled());

        worker.accept(work);
    }

    private static class Work {
        final ChunkPos columnPos;
        final CompletableFuture<ColumnData> future;

        Work(ChunkPos columnPos, CompletableFuture<ColumnData> future) {
            this.columnPos = columnPos;
            this.future = future;
        }

        void complete(ColumnData data) {
            this.future.complete(data);
        }

        void complete(Throwable throwable) {
            this.future.completeExceptionally(throwable);
        }

        void cancel() {
            this.future.cancel(true);
        }

        boolean isCancelled() {
            return this.future.isCancelled();
        }
    }

    private class Worker extends Thread {
        Worker(int index) {
            this.setName("terrarium-data-worker-" + index);
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (DistributedColumnLoader.this.active) {
                try {
                    DistributedColumnLoader.this.processWork(work -> {
                        try {
                            ColumnData data = DistributedColumnLoader.this.generate(work.columnPos);
                            work.complete(data);
                        } catch (Throwable t) {
                            work.complete(t);
                        }
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}
