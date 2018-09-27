package net.gegy1000.terrarium.server.world.generator.customization.property;

import javax.annotation.Nullable;

public interface CycleEnumProperty {
    String getKey();

    String getTranslationKey();

    @Nullable
    default String getDescriptionKey() {
        return null;
    }
}
