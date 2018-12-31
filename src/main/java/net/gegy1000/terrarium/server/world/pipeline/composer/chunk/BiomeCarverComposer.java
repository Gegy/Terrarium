package net.gegy1000.terrarium.server.world.pipeline.composer.chunk;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.class_2919;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.BitSet;
import java.util.List;

public class BiomeCarverComposer<C extends TerrariumGeneratorConfig> implements ChunkComposer<C> {
    private final RegionComponentType<BiomeRasterTile> biomeComponent;
    private final GenerationStep.Carver step;

    public BiomeCarverComposer(RegionComponentType<BiomeRasterTile> biomeComponent, GenerationStep.Carver step) {
        this.biomeComponent = biomeComponent;
        this.step = step;
    }

    @Override
    public void compose(ChunkGenerator<C> generator, Chunk chunk, class_2919 random, RegionGenerationHandler regionHandler) {
        ChunkPos pos = chunk.getPos();

        BiomeRasterTile biomeTile = regionHandler.getCachedChunkRaster(this.biomeComponent);
        Biome biome = biomeTile.get(0, 0);

        List<ConfiguredCarver<?>> carvers = biome.getCarversForStep(this.step);
        BitSet bitSet = chunk.method_12025(this.step);

        long seed = random.nextLong();

        for (int x = pos.x - 8; x <= pos.x + 8; x++) {
            for (int z = pos.z - 8; z <= pos.z + 8; z++) {
                for (int i = 0; i < carvers.size(); i++) {
                    ConfiguredCarver<?> carver = carvers.get(i);
                    random.method_12663(seed + i * 31, x, z);
                    if (carver.method_12669(random, x, z)) {
                        carver.method_12668(chunk, random, generator.method_16398(), x, z, pos.x, pos.z, bitSet);
                    }
                }
            }
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
