package net.gegy1000.terrarium.mixin;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import net.gegy1000.terrarium.server.world.customization.TerrariumPresetRegistry;
import net.minecraft.class_3807;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.util.UserCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(MinecraftDedicatedServer.class)
public class MixinDedicatedServer {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(File root, class_3807 properties, DataFixer dataFixer, YggdrasilAuthenticationService authService, MinecraftSessionService sessionService, GameProfileRepository profileRepo, UserCache userCache, CallbackInfo callback) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        server.getDataManager().addListener(TerrariumPresetRegistry.INSTANCE);
    }
}
