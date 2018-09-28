package net.gegy1000.earth.server.world;

import net.gegy1000.terrarium.server.world.generator.customization.property.CycleEnumProperty;
import net.minecraft.util.text.TextFormatting;

public enum FeatureGenerationFormat implements CycleEnumProperty {
    NONE("none", TextFormatting.RED),
    VANILLA("vanilla", TextFormatting.YELLOW),
    CUSTOM("custom", TextFormatting.YELLOW);

    private final String key;
    private final TextFormatting formatting;

    FeatureGenerationFormat(String key, TextFormatting formatting) {
        this.key = key;
        this.formatting = formatting;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getTranslationKey() {
        return "property.earth.feature_generation_" + this.key + ".name";
    }

    @Override
    public String getDescriptionKey() {
        return "property.earth.feature_generation_" + this.key + ".desc";
    }

    @Override
    public TextFormatting getFormatting() {
        return this.formatting;
    }

    public boolean isNone() {
        return this == NONE;
    }

    public CubicGenerationFormat toCubic(boolean cubicWorld) {
        switch (this) {
            case VANILLA:
                return CubicGenerationFormat.VANILLA;
            case CUSTOM:
                if (cubicWorld) {
                    return CubicGenerationFormat.CUBIC;
                } else {
                    return CubicGenerationFormat.VANILLA;
                }
            default:
                throw new IllegalStateException("Cannot coerce disabled generation into a cubic format!");
        }
    }
}
