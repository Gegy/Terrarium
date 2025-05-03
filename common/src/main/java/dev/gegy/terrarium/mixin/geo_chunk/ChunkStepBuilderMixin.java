package dev.gegy.terrarium.mixin.geo_chunk;

import dev.gegy.terrarium.world.chunk.ChunkStatusDecorator;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkTaskDispatcher;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.util.StaticCache2D;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTask;
import net.minecraft.world.level.chunk.status.ChunkStep;
import net.minecraft.world.level.chunk.status.WorldGenContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ChunkStep.Builder.class)
public class ChunkStepBuilderMixin {
    @Shadow
    @Final
    private ChunkStatus status;
    @Shadow
    private ChunkStatusTask task;

    @Inject(method = "build", at = @At("HEAD"))
    private void build(final CallbackInfoReturnable<ChunkStep> ci) {
        final ChunkStatusDecorator.Task injectedTask = ChunkStatusDecorator.getTaskToInject(status);
        if (injectedTask == null) {
            return;
        }
        final ChunkStatusTask currentTask = task;
        task = (context, step, chunkCache, chunk) -> {
            final CompletableFuture<?> result = injectedTask.run(context, chunkCache, chunk);
            if (result.isDone()) {
                return currentTask.doWork(context, step, chunkCache, chunk);
            }
            // The injected task will complete from another thread, so we need to schedule the rest of the work back on
            // the chunk generation executor
            return result.thenComposeAsync(
                    v -> currentTask.doWork(context, step, chunkCache, chunk),
                    terrarium$getWorldGenExecutor(context, chunkCache, chunk.getPos())
            );
        };
    }

    @Unique
    private static Executor terrarium$getWorldGenExecutor(final WorldGenContext context, final StaticCache2D<GenerationChunkHolder> chunkCache, final ChunkPos chunkPos) {
        final ChunkMap chunkMap = context.level().getChunkSource().chunkMap;
        final GenerationChunkHolder chunkHolder = chunkCache.get(chunkPos.x, chunkPos.z);
        final ChunkTaskDispatcher worldGenDispatcher = ((ChunkMapAccessor) chunkMap).getWorldgenTaskDispatcher();
        return command -> worldGenDispatcher.submit(command, chunkPos.toLong(), chunkHolder::getQueueLevel);
    }
}
