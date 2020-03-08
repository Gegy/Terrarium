package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.server.world.generator.CompositeTerrariumGenerator;

public interface TerrariumGeneratorInitializer {
    void setup(CompositeTerrariumGenerator.Builder builder);
}
