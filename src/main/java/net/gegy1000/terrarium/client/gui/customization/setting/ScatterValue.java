package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class ScatterValue extends AbstractCustomizationValue<Double> {
    public ScatterValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.scatter.name", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Double value) {
        settings.scatterRange = value.intValue();
    }

    @Override
    protected Double get(EarthGenerationSettings settings) {
        return (double) settings.scatterRange;
    }
}
