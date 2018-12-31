package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.class_2919;
import net.minecraft.class_3233;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class BiomeFeatureComposer<C extends TerrariumGeneratorConfig> implements DecorationComposer<C> {
    private final RegionComponentType<BiomeRasterTile> biomeComponent;

    public BiomeFeatureComposer(RegionComponentType<BiomeRasterTile> biomeComponent) {
        this.biomeComponent = biomeComponent;
    }

    @Override
    public void compose(ChunkGenerator<C> generator, class_3233 region, class_2919 random, RegionGenerationHandler regionHandler) {
        BiomeRasterTile biomeTile = regionHandler.getCachedChunkRaster(this.biomeComponent);
        Biome biome = biomeTile.get(0, 0);

        int chunkX = region.method_14336();
        int chunkZ = region.method_14339();
        BlockPos pos = new BlockPos(chunkX << 4, 0, chunkZ << 4);

        long seed = random.nextLong();
        for (GenerationStep.Feature step : GenerationStep.Feature.values()) {
            biome.generateFeatureStep(step, generator, region, seed, random, pos);
        }
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
