package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class StreetsValue extends AbstractCustomizationValue<Boolean> {
    public StreetsValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.streets", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Boolean value) {
        settings.streets = value;
    }

    @Override
    protected Boolean get(EarthGenerationSettings settings) {
        return settings.streets;
    }
}
