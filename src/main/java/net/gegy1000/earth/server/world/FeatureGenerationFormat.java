package net.gegy1000.earth.server.world;

import net.gegy1000.terrarium.server.world.generator.customization.property.CycleEnumProperty;

public enum FeatureGenerationFormat implements CycleEnumProperty {
    NONE("none"),
    VANILLA("vanilla"),
    CUSTOM("custom");

    private final String key;

    FeatureGenerationFormat(String key) {
        this.key = key;
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
}
