package net.gegy1000.terrarium.mixin.client;

import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.api.LevelGeneratorChooser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.gui.menu.NewLevelGui$9")
public class MixinCustomizeButton {
    @Shadow(aliases = "field_3214")
    @Final
    private NewLevelGui parent;

    @Inject(method = "onPressed", at = @At("HEAD"), cancellable = true)
    private void onPressed(double mouseX, double mouseY, CallbackInfo info) {
        LevelGeneratorChooser chooser = (LevelGeneratorChooser) this.parent;
        LevelGeneratorType generatorType = LevelGeneratorType.TYPES[chooser.getChosenIndex()];

        CustomLevelGenerator customGenerator = CustomLevelGenerator.unwrap(generatorType);
        if (customGenerator != null && generatorType.isCustomizable()) {
            Gui gui = customGenerator.createCustomizationGui(this.parent);
            if (gui != null) {
                MinecraftClient.getInstance().openGui(gui);
                info.cancel();
            }
        }
    }
}
