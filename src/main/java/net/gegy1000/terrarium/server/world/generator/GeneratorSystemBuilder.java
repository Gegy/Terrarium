package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.RegionDataSystem;

public interface GeneratorSystemBuilder {
    RegionDataSystem build(TerrariumGenerator generator, GenerationSettings settings);
}
