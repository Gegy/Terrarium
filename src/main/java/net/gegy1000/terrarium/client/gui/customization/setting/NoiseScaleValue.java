package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class NoiseScaleValue extends AbstractCustomizationValue<Double> {
    public NoiseScaleValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.noise_scale", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Double value) {
        settings.noiseScale = value;
    }

    @Override
    protected Double get(EarthGenerationSettings settings) {
        return settings.noiseScale;
    }
}
