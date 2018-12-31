package net.gegy1000.terrarium.server.world.pipeline.composer.chunk;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.class_2919;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public interface ChunkComposer<C extends TerrariumGeneratorConfig> {
    void compose(ChunkGenerator<C> generator, Chunk chunk, class_2919 random, RegionGenerationHandler regionHandler);

    RegionComponentType<?>[] getDependencies();
}
