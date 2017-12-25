package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class BuildingsValue extends AbstractCustomizationValue<Boolean> {
    public BuildingsValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.buildings.name", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Boolean value) {
        settings.buildings = value;
    }

    @Override
    protected Boolean get(EarthGenerationSettings settings) {
        return settings.buildings;
    }
}
