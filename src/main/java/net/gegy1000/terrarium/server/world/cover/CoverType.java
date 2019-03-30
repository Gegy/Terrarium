package net.gegy1000.terrarium.server.world.cover;

import net.minecraft.world.biome.Biome;

import java.awt.Color;

public interface CoverType<T extends CoverGenerationContext> {
    CoverSurfaceGenerator<T> createSurfaceGenerator(T context);

    CoverDecorationGenerator<T> createDecorationGenerator(T context);

    Biome getBiome(T context, int x, int z);

    default Color getApproximateColor() {
        return Color.WHITE;
    }
}
