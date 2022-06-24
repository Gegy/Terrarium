package dev.gegy.terrarium.world.chunk;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ChunkStatusDecorator {
    static void runBefore(final ChunkStatus status, final Task task) {
        ((ChunkStatusDecorator) status).runBefore(task);
    }

    void runBefore(Task task);

    interface Task {
        CompletableFuture<?> run(ServerLevel level, ChunkGenerator generator, ChunkAccess chunk, List<ChunkAccess> context);
    }
}
