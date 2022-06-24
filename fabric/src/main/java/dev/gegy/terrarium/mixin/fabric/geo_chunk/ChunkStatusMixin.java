package dev.gegy.terrarium.mixin.fabric.geo_chunk;

import dev.gegy.terrarium.world.chunk.ChunkStatusDecorator;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkStatus.class)
public class ChunkStatusMixin implements ChunkStatusDecorator {
    @Mutable
    @Shadow
    @Final
    private ChunkStatus.GenerationTask generationTask;

    @Override
    public void runBefore(final Task task) {
        final ChunkStatus.GenerationTask currentTask = generationTask;
        generationTask = (status, executor, level, generator, templateManager, lightEngine, upgradeToFullChunk, context, chunk) ->
                task.run(level, generator, chunk, context)
                        .thenComposeAsync(v -> currentTask.doWork(status, executor, level, generator, templateManager, lightEngine, upgradeToFullChunk, context, chunk), executor);
    }
}
