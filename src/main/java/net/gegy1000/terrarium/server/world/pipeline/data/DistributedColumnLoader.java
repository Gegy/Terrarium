package net.gegy1000.terrarium.server.world.pipeline.data;

import com.google.common.collect.Maps;
import net.minecraft.util.math.ChunkPos;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

// TODO: It might be valid to replace this with a direct on-thread loader
public final class DistributedColumnLoader implements ColumnDataLoader {
    private static final int WORKER_COUNT = 1;

    private final Function<ChunkPos, ColumnData> generator;

    private final Worker[] workers = new Worker[WORKER_COUNT];

    private final LinkedBlockingQueue<Work> workQueue = new LinkedBlockingQueue<>();
    private final Map<ChunkPos, Work> unseenWorkMap = Maps.newConcurrentMap();

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
        this.unseenWorkMap.put(columnPos, work);

        return future;
    }

    @Override
    public ColumnData get(ChunkPos columnPos) {
        Work unseenWork = this.unseenWorkMap.get(columnPos);
        if (unseenWork != null) {
            this.cancelWork(unseenWork);
            return this.generate(columnPos);
        }

        return this.getAsync(columnPos).join();
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

    void cancelWork(Work work) {
        work.cancel();
        this.unseenWorkMap.remove(work.columnPos);
    }

    void processWork(Consumer<Work> worker) throws InterruptedException {
        Work work;
        do {
            work = this.workQueue.take();
            this.unseenWorkMap.remove(work.columnPos);
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
