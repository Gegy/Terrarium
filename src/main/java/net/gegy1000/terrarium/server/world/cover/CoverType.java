package net.gegy1000.terrarium.server.world.cover;

import net.minecraft.world.biome.Biome;

public interface CoverType<T extends CoverGenerationContext> {
    CoverSurfaceGenerator<T> createSurfaceGenerator(T context);

    CoverDecorationGenerator<T> createDecorationGenerator(T context);

    Biome getBiome(int x, int z);

    Class<T> getRequiredContext();
}
