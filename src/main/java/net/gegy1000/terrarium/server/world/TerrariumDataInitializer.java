package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.server.world.data.DataGenerator;

public interface TerrariumDataInitializer {
    void setup(DataGenerator.Builder builder);
}
