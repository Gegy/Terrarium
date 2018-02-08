package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class BeachSizeValue extends AbstractCustomizationValue<Double> {
    public BeachSizeValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.beach_size", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Double value) {
        settings.beachSize = value.intValue();
    }

    @Override
    protected Double get(EarthGenerationSettings settings) {
        return (double) settings.beachSize;
    }
}
