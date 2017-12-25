package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class HeightScaleValue extends AbstractCustomizationValue<Double> {
    public HeightScaleValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.height_scale.name", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Double value) {
        settings.terrainHeightScale = value;
    }

    @Override
    protected Double get(EarthGenerationSettings settings) {
        return settings.terrainHeightScale;
    }
}
