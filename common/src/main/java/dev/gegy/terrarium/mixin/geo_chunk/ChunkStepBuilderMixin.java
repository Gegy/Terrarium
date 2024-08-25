package dev.gegy.terrarium.mixin.geo_chunk;

import dev.gegy.terrarium.world.chunk.ChunkStatusDecorator;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStatusTask;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        task = (context, step, chunkCache, chunk) -> injectedTask.run(context, chunkCache, chunk)
                .thenComposeAsync(v -> currentTask.doWork(context, step, chunkCache, chunk));
    }
}
