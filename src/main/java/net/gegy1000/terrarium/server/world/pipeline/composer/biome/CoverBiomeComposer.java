package net.gegy1000.terrarium.server.world.pipeline.composer.biome;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.cover.ConstructedCover;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTile;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoverBiomeComposer implements BiomeComposer {
    private final RegionComponentType<CoverRasterTile> coverComponent;
    private final Map<CoverType<?>, CoverGenerationContext> context;

    private final Biome[] biomeBuffer = new Biome[16 * 16];

    public CoverBiomeComposer(
            RegionComponentType<CoverRasterTile> coverComponent,
            List<ConstructedCover<?>> coverTypes
    ) {
        this.coverComponent = coverComponent;
        this.context = coverTypes.stream().collect(Collectors.toMap(ConstructedCover::getType, ConstructedCover::getContext));
    }

    @Override
    public Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        CoverRasterTile coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                CoverType<?> coverType = coverRaster.get(localX, localZ);
                this.biomeBuffer[localX + localZ * 16] = this.getBiome(coverType, globalX + localX, globalZ + localZ);
            }
        }

        return this.biomeBuffer;
    }

    @SuppressWarnings("unchecked")
    private <T extends CoverGenerationContext> Biome getBiome(CoverType<T> coverType, int globalX, int globalZ) {
        CoverGenerationContext context = this.context.get(coverType);
        if (context != null) {
            return coverType.getBiome((T) context, globalX, globalZ);
        } else {
            Terrarium.LOGGER.warn("Tried to get biome for non-registered cover type: {}", coverType);
        }
        return Biomes.DEFAULT;
    }
}
