package net.gegy1000.earth.server.world.cover.type;

import net.gegy1000.earth.server.world.cover.EarthCoverContext;
import net.gegy1000.earth.server.world.cover.EarthCoverType;
import net.gegy1000.terrarium.server.world.cover.CoverDecorationGenerator;
import net.gegy1000.terrarium.server.world.cover.CoverSurfaceGenerator;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public class UrbanCover extends EarthCoverType {
    @Override
    public CoverSurfaceGenerator<EarthCoverContext> createSurfaceGenerator(EarthCoverContext context) {
        return new CoverSurfaceGenerator.Inherit<>(context, this);
    }

    @Override
    public CoverDecorationGenerator<EarthCoverContext> createDecorationGenerator(EarthCoverContext context) {
        return new CoverDecorationGenerator.Empty<>(context, this);
    }

    @Override
    public Biome getBiome(EarthCoverContext context, int x, int z) {
        return Biomes.PLAINS;
    }
}
