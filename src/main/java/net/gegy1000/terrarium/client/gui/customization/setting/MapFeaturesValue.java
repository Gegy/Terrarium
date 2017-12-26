package net.gegy1000.terrarium.client.gui.customization.setting;

import net.gegy1000.terrarium.server.world.EarthGenerationSettings;

public class MapFeaturesValue extends AbstractCustomizationValue<Boolean> {
    public MapFeaturesValue(EarthGenerationSettings settings, Runnable onChanged) {
        super("setting.terrarium.map_features", settings, onChanged);
    }

    @Override
    protected void set(EarthGenerationSettings settings, Boolean value) {
        settings.mapFeatures = value;
    }

    @Override
    protected Boolean get(EarthGenerationSettings settings) {
        return settings.mapFeatures;
    }
}
