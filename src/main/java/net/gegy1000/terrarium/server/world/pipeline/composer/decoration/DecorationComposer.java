package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.class_2919;
import net.minecraft.class_3233;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public interface DecorationComposer<C extends TerrariumGeneratorConfig> {
    void compose(ChunkGenerator<C> generator, class_3233 region, class_2919 random, RegionGenerationHandler regionHandler);

    RegionComponentType<?>[] getDependencies();
}
