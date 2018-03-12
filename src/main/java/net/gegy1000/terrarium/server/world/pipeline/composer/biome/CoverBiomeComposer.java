package net.gegy1000.terrarium.server.world.pipeline.composer.biome;

import com.google.gson.JsonObject;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.LatitudinalZone;
import net.gegy1000.terrarium.server.world.json.InstanceJsonValueParser;
import net.gegy1000.terrarium.server.world.json.InstanceObjectParser;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.CoverRasterTileAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class CoverBiomeComposer implements BiomeComposer {
    private final RegionComponentType<CoverRasterTileAccess> coverComponent;

    private final Biome[] biomeBuffer = new Biome[16 * 16];

    public CoverBiomeComposer(RegionComponentType<CoverRasterTileAccess> coverComponent) {
        this.coverComponent = coverComponent;
    }

    @Override
    public Biome[] getBiomes(GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
        CoverRasterTileAccess coverRaster = regionHandler.getCachedChunkRaster(this.coverComponent);
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                // TODO: Handle latitudinal zones
                this.biomeBuffer[localX + localZ * 16] = coverRaster.get(localX, localZ).getBiome(LatitudinalZone.TROPICS);
            }
        }

        return this.biomeBuffer;
    }

    public static class Parser implements InstanceObjectParser<BiomeComposer> {
        @Override
        public BiomeComposer parse(TerrariumWorldData worldData, World world, InstanceJsonValueParser valueParser, JsonObject objectRoot) {
            RegionComponentType<CoverRasterTileAccess> coverComponent = valueParser.parseComponentType(objectRoot, "cover_component", CoverRasterTileAccess.class);
            return new CoverBiomeComposer(coverComponent);
        }
    }
}
