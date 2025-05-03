package dev.gegy.terrarium.mixin.fabric.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.gegy.terrarium.client.screen.ConfigureEarthScreen;
import dev.gegy.terrarium.registry.TerrariumWorldPresets;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.Optional;

@Mixin(PresetEditor.class)
public interface PresetEditorMixin {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/Map;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/Map;"))
    private static Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> init(final Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> map) {
        return Util.copyAndPut(map,
                Optional.of(TerrariumWorldPresets.EARTH), ConfigureEarthScreen::create
        );
    }
}
