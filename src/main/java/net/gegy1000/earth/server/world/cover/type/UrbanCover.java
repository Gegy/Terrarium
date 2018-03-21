package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class UrbanCover implements CoverType {
    @Override
    public CoverSurfaceGenerator createSurfaceGenerator(CoverGenerationContext context) {
        return new CoverSurfaceGenerator.Inherit(context, this);
    }

    @Override
    public CoverDecorationGenerator createDecorationGenerator(CoverGenerationContext context) {
        return new CoverDecorationGenerator.Empty(context, this);
    }

    @Override
    public Biome getBiome(int x, int z) {
        return Biomes.PLAINS;
    }
}
