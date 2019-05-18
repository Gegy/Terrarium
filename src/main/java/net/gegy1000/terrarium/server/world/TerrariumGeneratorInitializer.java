package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;

public interface TerrariumGeneratorInitializer {
    TerrariumGenerator buildGenerator(boolean preview);
}
