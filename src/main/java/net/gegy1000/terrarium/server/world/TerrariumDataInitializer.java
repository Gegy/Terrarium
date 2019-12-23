package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.server.world.data.ColumnDataGenerator;

public interface TerrariumDataInitializer {
    void setup(ColumnDataGenerator.Builder builder);
}
