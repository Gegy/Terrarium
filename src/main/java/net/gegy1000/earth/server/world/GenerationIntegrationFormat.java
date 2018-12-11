package net.gegy1000.earth.server.world;

import net.gegy1000.terrarium.server.world.generator.customization.property.CycleEnumProperty;
import net.minecraft.util.text.TextFormatting;

public enum GenerationIntegrationFormat implements CycleEnumProperty {
    NONE("none", TextFormatting.RED),
    CUSTOM("custom", TextFormatting.YELLOW),
    VANILLA("vanilla", TextFormatting.YELLOW);

    private final String key;
    private final TextFormatting formatting;

    GenerationIntegrationFormat(String key, TextFormatting formatting) {
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

    public boolean shouldGenerate() {
        return this != NONE;
    }

    public CubicIntegrationFormat toCubic(boolean cubicWorld) {
        switch (this) {
            case VANILLA:
                return CubicIntegrationFormat.VANILLA;
            case CUSTOM:
                return cubicWorld ? CubicIntegrationFormat.CUBIC : CubicIntegrationFormat.VANILLA;
            default:
                throw new IllegalStateException("Cannot coerce disabled generation into a cubic format!");
        }
    }
}
