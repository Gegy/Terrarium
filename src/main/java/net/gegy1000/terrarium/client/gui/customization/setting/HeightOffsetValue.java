package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class HeightOffsetValue extends AbstractCustomizationValue<Double> {
    public HeightOffsetValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.height_offset", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Double value) {
        settings.heightOffset = value.intValue();
    }

    @Override
    protected Double get(EarthGenerationSettings settings) {
        return (double) settings.heightOffset;
    }
}
