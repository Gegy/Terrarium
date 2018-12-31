package net.gegy1000.terrarium.mixin.client;

import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.api.LevelGeneratorChooser;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NewLevelGui.class)
public class MixinNewLevelGui implements LevelGeneratorChooser {
    @Shadow
    private int generatorType;

    @Shadow
    private ButtonWidget buttonGenerateStructures;

    @Override
    public int getChosenIndex() {
        return this.generatorType;
    }

    @Inject(method = "method_2723", at = @At("HEAD"), cancellable = true)
    private void canSwitchTo(CallbackInfoReturnable<Boolean> callback) {
        LevelGeneratorType generatorType = LevelGeneratorType.TYPES[this.generatorType];
        CustomLevelGenerator customGenerator = CustomLevelGenerator.unwrap(generatorType);
        if (customGenerator != null && customGenerator.isVisible()) {
            boolean hidden = customGenerator.isHidden();
            callback.setReturnValue(!hidden || Gui.isShiftPressed());
        }
    }

    @Inject(method = "method_2710", at = @At("RETURN"))
    private void updateButtonVisibility(CallbackInfo callback) {
        LevelGeneratorType generatorType = LevelGeneratorType.TYPES[this.generatorType];
        CustomLevelGenerator customGenerator = CustomLevelGenerator.unwrap(generatorType);
        if (customGenerator != null && !customGenerator.canToggleStructures()) {
            this.buttonGenerateStructures.visible = false;
        }
    }
}
