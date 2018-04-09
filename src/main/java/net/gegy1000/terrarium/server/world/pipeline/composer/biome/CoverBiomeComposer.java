package net.gegy1000.terrarium.server.world.pipeline.composer.biome;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.cover.CoverGenerationContext;
import net.gegy1000.terrarium.server.world.cover.CoverType;
import net.gegy1000.terrarium.server.world.cover.DeclaredCoverTypeParser;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.json.InvalidJsonException;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;

public class CoverBiomeComposer implements BiomeComposer {
    private final RegionComponentType<CoverRasterTileAccess> coverComponent;
    private final Map<CoverType<?>, CoverGenerationContext> context;

    private final Biome[] biomeBuffer = new Biome[16 * 16];

    public CoverBiomeComposer(
            RegionComponentType<CoverRasterTileAccess> coverComponent,
            Map<CoverType<?>, CoverGenerationContext> context
    ) {
        this.coverComponent = coverComponent;
        this.context = context;
    }

    @Override
    public Biome[] getBiomes(GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        CoverRasterTileAccess coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);
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
            if (coverType.getRequiredContext().isAssignableFrom(context.getClass())) {
                return coverType.getBiome((T) context, globalX, globalZ);
            }
        } else {
            Terrarium.LOGGER.warn("Tried to get biome for non-registered cover type: {}", coverType);
        }
        return Biomes.DEFAULT;
    }

    public static class Parser implements InstanceObjectParser<BiomeComposer> {
        @Override
        public BiomeComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) throws InvalidJsonException {
            RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);

            Map<CoverType<?>, CoverGenerationContext> contexts = new HashMap<>();
            DeclaredCoverTypeParser.parseCoverTypes(objectRoot, valueParser, contexts::put);

            return new CoverBiomeComposer(coverComponent, contexts);
        }
    }
}
