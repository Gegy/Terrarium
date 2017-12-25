package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class ScaleValue extends AbstractCustomizationValue<Double> {
    public ScaleValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.scale.name", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Double value) {
        settings.worldScale = 1.0 / value;
    }

    @Override
    protected Double get(EarthGenerationSettings settings) {
        return 1.0 / settings.worldScale;
    }
}
