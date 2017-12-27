package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class ResourceGenerationValue extends AbstractCustomizationValue<Boolean> {
    public ResourceGenerationValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.resource_generation", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Boolean value) {
        settings.resourceGeneration = value;
    }

    @Override
    protected Boolean get(EarthGenerationSettings settings) {
        return settings.resourceGeneration;
    }
}
