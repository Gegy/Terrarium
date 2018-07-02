package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.server.world.generator.TerrariumGenerator;
import net.gegy1000.terrarium.server.world.pipeline.TerrariumDataProvider;

public interface TerrariumGeneratorInitializer {
    TerrariumGenerator buildGenerator();

    TerrariumDataProvider buildDataProvider();
}
