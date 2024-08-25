package dev.gegy.terrarium.world.chunk;

import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChunkStatusDecorator {
    private static final Map<ChunkStatus, Task> TASKS = new HashMap<>();
    private static boolean initializedPyramid;

    public static void runBefore(final ChunkStatus status, final Task task) {
        if (initializedPyramid) {
            throw new IllegalStateException("ChunkPyramid has already been initialized");
        }
        TASKS.put(status, task);
    }

    @Nullable
    public static Task getTaskToInject(final ChunkStatus status) {
        initializedPyramid = true;
        return TASKS.get(status);
    }

    public interface Task {
        CompletableFuture<?> run(WorldGenContext context, StaticCache2D<GenerationChunkHolder> chunkCache, ChunkAccess chunk);
    }
}
