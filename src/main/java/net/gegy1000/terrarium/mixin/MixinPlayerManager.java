package net.gegy1000.terrarium.mixin;

import net.gegy1000.terrarium.server.event.PlayerEvent;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {
    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo callback) {
        PlayerEvent.dispatch(PlayerEvent.CONNECT, player);
    }

    @Inject(method = "method_14611", at = @At("HEAD"))
    private void onPlayerDisconnect(ServerPlayerEntity player, CallbackInfo callback) {
        PlayerEvent.dispatch(PlayerEvent.DISCONNECT, player);
    }
}
