package net.gegy1000.earth.server.world.pipeline.composer;

import net.gegy1000.earth.server.world.biome.FakeBiome;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.BiomeRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.world.biome.Biome;

public class EarthBiomeComposer implements BiomeComposer {
    private final RegionComponentType<BiomeRasterTile> biomeComponent;

    private final Biome[] biomeBuffer = new Biome[16 * 16];

    public EarthBiomeComposer(RegionComponentType<BiomeRasterTile> biomeComponent) {
        this.biomeComponent = biomeComponent;
    }

    @Override
    public Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        BiomeRasterTile biomeRaster = regionHandler.getCachedChunkRaster(this.biomeComponent);
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                // TODO: This can be moved to Terrarium package
                Biome biome = biomeRaster.get(localX, localZ);
                if (biome instanceof FakeBiome) {
                    // TODO: Context! Real equivalents change
                    this.biomeBuffer[localX + localZ * 16] = ((FakeBiome) biome).getRealEquivalent();
                } else {
                    this.biomeBuffer[localX + localZ * 16] = biome;
                }
            }
        }

        return this.biomeBuffer;
    }

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[] { this.biomeComponent };
    }
}
