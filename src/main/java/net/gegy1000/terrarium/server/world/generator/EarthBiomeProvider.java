package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.GenerationRegionHandler;
import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.map.system.chunk.CoverChunkDataProvider;
import net.gegy1000.terrarium.server.map.system.component.TerrariumComponentTypes;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public class EarthBiomeProvider extends CoveredBiomeProvider {
    private final EarthGenerationSettings settings;

    private final PseudoRandomMap zoneScatterMap;

    private final Lazy<GenerationRegionHandler> regionHandler = new Lazy<>(() -> {
        TerrariumWorldData capability = this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (capability != null) {
            return capability.getRegionHandler();
        }
        throw new RuntimeException("Tried to load EarthGenerationHandler before it was present");
    });

    private final CoverChunkDataProvider coverProvider;

    public EarthBiomeProvider(World world) {
        super(world);

        this.settings = EarthGenerationSettings.deserialize(world.getWorldInfo().getGeneratorOptions());
        this.zoneScatterMap = new PseudoRandomMap(world, EarthChunkGenerator.ZONE_SCATTER_SEED);

        this.coverProvider = new CoverChunkDataProvider(this.settings, world, TerrariumComponentTypes.COVER);
    }

    @Override
    protected void populateChunk(Biome[] biomes, int x, int z) {
        this.coverProvider.populate(this.regionHandler.get(), this.world, x, z);
        CoverType[] coverData = this.coverProvider.getResultStore().getCoverData();

        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int index = localX + localZ * 16;
                biomes[index] = coverData[index].getBiome(this.getLatitudinalZone(x + localX, z + localZ));
            }
        }
    }

    private LatitudinalZone getLatitudinalZone(int x, int z) {
        int offset = this.zoneScatterMap.nextInt(128) - this.zoneScatterMap.nextInt(128);
        Coordinate chunkCoordinate = Coordinate.fromBlock(this.settings, x, z + offset);
        return LatitudinalZone.get(chunkCoordinate);
    }
}
