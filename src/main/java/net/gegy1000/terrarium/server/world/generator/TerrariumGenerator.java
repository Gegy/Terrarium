package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.world.coordinate.Coordinate;

public interface TerrariumGenerator {
    ChunkCompositionProcedure getCompositionProcedure();

    Coordinate getSpawnPosition();
}
