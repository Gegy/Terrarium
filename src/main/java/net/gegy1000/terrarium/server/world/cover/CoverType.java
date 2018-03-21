package net.gegy1000.terrarium.server.world.cover;

import net.minecraft.world.biome.Biome;

public interface CoverType {
    CoverSurfaceGenerator createSurfaceGenerator(CoverGenerationContext context);

    CoverDecorationGenerator createDecorationGenerator(CoverGenerationContext context);

    Biome getBiome(int x, int z);
}
