package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class CaveGenValue extends AbstractCustomizationValue<Boolean> {
    public CaveGenValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.cave_gen", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Boolean value) {
        settings.caveGeneration = value;
    }

    @Override
    protected Boolean get(EarthGenerationSettings settings) {
        return settings.caveGeneration;
    }
}
