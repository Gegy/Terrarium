package net.gegy1000.earth.server.world.data;

import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.ChunkedIterator;
import net.gegy1000.terrarium.server.util.FutureUtil;
import net.gegy1000.terrarium.server.world.data.DataGenerator;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class DataPreloadManager {
    private static final int BATCH_SIZE = 100;

    private final DataGenerator generator;
    private final ChunkPos min;
    private final ChunkPos max;

    private DataPreloadManager(DataGenerator generator, ChunkPos min, ChunkPos max) {
        this.generator = generator;
        this.min = min;
        this.max = max;
    }

    public static DataPreloadManager open(TerrariumWorld terrarium, ChunkPos min, ChunkPos max) {
        return new DataPreloadManager(terrarium.getDataGenerator(), min, max);
    }

    public void start(Watcher watcher) {
        long width = (this.max.x - this.min.x) + 1;
        long height = (this.max.z - this.min.z) + 1;
        long total = width * height;

        watcher.update(0, total);

        Thread thread = new Thread(() -> {
            long count = 0;

            Iterable<Collection<BlockPos>> chunks = ChunkedIterator.of(BlockPos.getAllInBox(
                    this.min.x, 0, this.min.z,
                    this.max.x, 0, this.max.z
            ), BATCH_SIZE);

            for (Collection<BlockPos> chunk : chunks) {
                Stream<CompletableFuture<Void>> futures = chunk.stream()
                        .map(pos -> {
                            DataView view = DataView.square(pos.getX() << 4, pos.getZ() << 4, 16);
                            return this.generator.preload(view);
                        });

                FutureUtil.allOf(futures).join();

                count += chunk.size();
                watcher.update(count, total);
            }
        });
        thread.setName("data-preload-manager");
        thread.setDaemon(true);

        thread.start();
    }

    public interface Watcher {
        void update(long count, long total);
    }
}
