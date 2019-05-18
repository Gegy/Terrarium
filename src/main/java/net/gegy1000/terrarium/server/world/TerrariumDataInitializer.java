package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataGenerator;

public interface TerrariumDataInitializer {
    ColumnDataGenerator buildDataGenerator();
}
