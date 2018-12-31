package net.gegy1000.terrarium.server.world.pipeline.composer.decoration;

import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.class_2919;
import net.minecraft.class_3233;
import net.minecraft.sortme.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class BiomeSpawnComposer<C extends TerrariumGeneratorConfig> implements DecorationComposer<C> {
    private final RegionComponentType<BiomeRasterTile> biomeComponent;

    public BiomeSpawnComposer(RegionComponentType<BiomeRasterTile> biomeComponent) {
        this.biomeComponent = biomeComponent;
    }

    @Override
    public void compose(ChunkGenerator<C> generator, class_3233 region, class_2919 random, RegionGenerationHandler regionHandler) {
        int chunkX = region.method_14336();
        int chunkZ = region.method_14339();

        BiomeRasterTile biomeTile = regionHandler.getCachedChunkRaster(this.biomeComponent);
        Biome biome = biomeTile.get(0, 0);

        SpawnHelper.method_8661(region, biome, chunkX, chunkZ, random);
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
