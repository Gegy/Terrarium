package net.gegy1000.terrarium.server.world.chunk;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public abstract class TerrariumChunkGenerator<C extends TerrariumGeneratorConfig> extends ChunkGenerator<C> {
    public TerrariumChunkGenerator(IWorld world, BiomeSource biomeSource, C settings) {
        super(world, biomeSource, settings);
    }

    @Override
    public int method_12100() {
        return this.world.getSeaLevel() + 1;
    }
}
