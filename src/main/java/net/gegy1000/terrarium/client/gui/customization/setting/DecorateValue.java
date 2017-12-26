package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class DecorateValue extends AbstractCustomizationValue<Boolean> {
    public DecorateValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.decorate", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Boolean value) {
        settings.decorate = value;
    }

    @Override
    protected Boolean get(EarthGenerationSettings settings) {
        return settings.decorate;
    }
}
