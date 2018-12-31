package net.gegy1000.earth.server.world.chunk;

import net.gegy1000.earth.server.world.EarthGeneratorConfig;
import net.gegy1000.terrarium.server.world.chunk.ComposableChunkGenerator;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.source.BiomeSource;

public class EarthChunkGenerator extends ComposableChunkGenerator<EarthGeneratorConfig> {
    public EarthChunkGenerator(IWorld world, BiomeSource biomeSource, EarthGeneratorConfig config) {
        super(world, biomeSource, config);
    }
}
