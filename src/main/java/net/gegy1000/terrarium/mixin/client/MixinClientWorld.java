package net.gegy1000.terrarium.mixin.client;

import net.gegy1000.terrarium.server.event.WorldEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Profiler;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinClientWorld {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(ClientPlayNetworkHandler networkHandler, LevelInfo info, DimensionType dimension, Difficulty difficulty, Profiler profiler, CallbackInfo callback) {
        WorldEvent.dispatch(WorldEvent.LOAD, (World) (Object) this);
    }
}
