package net.gegy1000.terrarium.mixin;

import net.gegy1000.terrarium.server.event.WorldEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Profiler;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public class MixinServerWorld {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(MinecraftServer server, Executor executor, WorldSaveHandler saveHandler, PersistentStateManager stateManager, LevelProperties properties, DimensionType dimension, Profiler profiler, CallbackInfo callback) {
        WorldEvent.dispatch(WorldEvent.LOAD, (World) (Object) this);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void close(CallbackInfo callback) {
        WorldEvent.dispatch(WorldEvent.UNLOAD, (World) (Object) this);
    }
}
