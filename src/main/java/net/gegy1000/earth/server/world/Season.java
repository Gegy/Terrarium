package net.gegy1000.earth.server.world;

import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.world.data.source.WorldClimateRaster;
import net.gegy1000.terrarium.server.world.generator.customization.property.CycleEnumProperty;

public enum Season implements CycleEnumProperty {
    JANUARY("january"),
    JULY("july");

    private final String key;

    Season(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getTranslationKey() {
        return "property.earth.season." + this.key;
    }

    public WorldClimateRaster getClimateRaster() {
        SharedEarthData sharedData = SharedEarthData.instance();
        if (this == JANUARY) {
            return sharedData.get(SharedEarthData.JANUARY_CLIMATE);
        } else {
            return sharedData.get(SharedEarthData.JULY_CLIMATE);
        }
    }
}
