package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.glob.GlobType;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.util.Lazy;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.layer.IntCache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EarthBiomeProvider extends BiomeProvider {
    private final World world;

    private final Lazy<EarthGenerationHandler> generationHandler = new Lazy<>(() -> {
        TerrariumWorldData capability = EarthBiomeProvider.this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (capability != null) {
            return capability.getGenerationHandler();
        }
        throw new RuntimeException("Tried to load EarthGenerationHandler before it was present");
    });

    private final BiomeCache biomeCache = new BiomeCache(this);
    private final List<Biome> spawnBiomes = new ArrayList<>(allowedBiomes);

    private final GlobType[] globBuffer = ArrayUtils.defaulted(new GlobType[256], GlobType.NO_DATA);

    public EarthBiomeProvider(World world) {
        this.world = world;
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return this.spawnBiomes;
    }

    @Override
    public Biome getBiome(BlockPos pos, Biome defaultBiome) {
        return this.biomeCache.getBiome(pos.getX(), pos.getZ(), defaultBiome);
    }

    @Override
    public float getTemperatureAtHeight(float biomeTemperature, int height) {
        return biomeTemperature;
    }

    @Override
    public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
        if (biomes.length < width * height) {
            biomes = new Biome[256];
        }
        Arrays.fill(biomes, Biomes.OCEAN);
        return biomes;
    }

    @Override
    public Biome[] getBiomes(Biome[] biomes, int x, int z, int width, int height, boolean cache) {
        IntCache.resetIntCache();
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }
        boolean fillsChunk = width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0;
        if (cache && fillsChunk) {
            System.arraycopy(this.biomeCache.getCachedBiomes(x, z), 0, biomes, 0, width * height);
            return biomes;
        } else if (fillsChunk) {
            GlobType[] globBuffer = this.globBuffer;
            this.generationHandler.get().populateGlobRegion(globBuffer, x >> 4, z >> 4);
            for (int i = 0; i < globBuffer.length; i++) {
                biomes[i] = globBuffer[i].getBiome();
            }
            return biomes;
        } else {
            GlobType[] globBuffer = this.globBuffer;
            int chunkMinX = x >> 4;
            int chunkMinZ = z >> 4;
            int chunkMaxX = (x + width) >> 4;
            int chunkMaxZ = (z + height) >> 4;
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                    this.generationHandler.get().populateGlobRegion(globBuffer, chunkX, chunkZ);
                    for (int localZ = Math.max(chunkZ << 4, z); localZ <= Math.min((chunkZ + 1) << 4, z + height); localZ++) {
                        for (int localX = Math.max(chunkX << 4, x); localX <= Math.min((chunkX + 1) << 4, x + width); localX++) {
                            int index = (localX - x) + (localZ - z) * 16;
                            biomes[index] = globBuffer[index].getBiome();
                        }
                    }
                }
            }
            return biomes;
        }
    }

    @Override
    public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
        return true;
    }

    // TODO: Implement properly
    @Override
    public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
        return new BlockPos(x, 0, z);
    }

    @Override
    public void cleanupCache() {
        this.biomeCache.cleanupCache();
    }
}
