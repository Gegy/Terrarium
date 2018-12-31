package net.gegy1000.terrarium.mixin;

import net.gegy1000.terrarium.api.CustomLevelGenerator;
import net.gegy1000.terrarium.api.ExtendedLevelGenerator;
import net.minecraft.world.level.LevelGeneratorType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(LevelGeneratorType.class)
public class MixinLevelGeneratorType implements ExtendedLevelGenerator {
    private CustomLevelGenerator source;

    @Override
    public void setSource(CustomLevelGenerator generator) {
        if (this.source != null) {
            throw new IllegalArgumentException("Level generator is already extended");
        }
        this.source = generator;
    }

    @Nullable
    @Override
    public CustomLevelGenerator getSource() {
        return this.source;
    }

    @Inject(method = "isCustomizable", at = @At("HEAD"), cancellable = true)
    private void isCustomizable(CallbackInfoReturnable<Boolean> callback) {
        if (this.source != null) {
            callback.setReturnValue(this.source.isCustomizable());
        }
    }

    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void isVisible(CallbackInfoReturnable<Boolean> callback) {
        if (this.source != null) {
            callback.setReturnValue(this.source.isVisible());
        }
    }
}
