package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;

public interface ChunkCompositionProcedure extends SurfaceComposer, DecorationComposer, BiomeComposer, StructureComposer {
}
