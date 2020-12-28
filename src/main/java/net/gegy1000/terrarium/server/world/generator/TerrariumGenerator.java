package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;

public interface TerrariumGenerator {
    SurfaceComposer getSurfaceComposer();

    DecorationComposer getDecorationComposer();

    StructureComposer getStructureComposer();

    BiomeComposer getBiomeComposer();

    Coordinate getSpawnPosition();
}
