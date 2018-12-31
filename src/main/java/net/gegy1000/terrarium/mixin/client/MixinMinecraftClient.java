package net.gegy1000.terrarium.mixin.client;

import net.gegy1000.terrarium.client.event.GuiChangeEvent;
import net.gegy1000.terrarium.server.event.WorldEvent;
import net.gegy1000.terrarium.server.world.customization.TerrariumPresetRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow
    public ClientWorld world;
    @Shadow
    private ReloadableResourceManager resourceManager;

    @ModifyVariable(method = "openGui", at = @At("HEAD"), index = 1)
    private Gui openGui(Gui newGui) {
        return GuiChangeEvent.dispatch(GuiChangeEvent.HANDLERS, newGui);
    }

    @Inject(method = "method_1550", at = @At("HEAD"))
    private void onChangeWorld(ClientWorld newWorld, Gui gui, CallbackInfo callback) {
        if (this.world != null) {
            WorldEvent.dispatch(WorldEvent.UNLOAD, this.world);
        }
    }

    @Inject(
            method = "init",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;viewport(IIII)V")
    )
    private void registerReloadListeners(CallbackInfo info) {
        this.resourceManager.addListener(TerrariumPresetRegistry.INSTANCE);
    }
}
