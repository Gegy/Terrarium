package net.gegy1000.terrarium.server.world.generator.customization.property;

import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public interface CycleEnumProperty {
    String getKey();

    String getTranslationKey();

    default TextFormatting getFormatting() {
        return TextFormatting.YELLOW;
    }

    @Nullable
    default String getDescriptionKey() {
        return null;
    }
}
