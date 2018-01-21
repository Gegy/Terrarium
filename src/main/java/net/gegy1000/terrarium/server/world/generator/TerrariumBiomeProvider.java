package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.util.ArrayUtils;
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

public abstract class TerrariumBiomeProvider extends BiomeProvider {
    protected final World world;

    protected final Random random = new Random();

    private final BiomeCache biomeCache = new BiomeCache(this);
    private final List<Biome> spawnBiomes = new ArrayList<>(allowedBiomes);

    private final CoverType[] coverBuffer = ArrayUtils.defaulted(new CoverType[256], CoverType.NO_DATA);

    public TerrariumBiomeProvider(World world) {
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
            return this.populateChunk(biomes, x, z);
        } else {
            this.populateArea(biomes, x, z, width, height);
            return biomes;
        }
    }

    private Biome[] populateChunk(Biome[] biomes, int x, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        this.initializeRandom(chunkX, chunkZ);
        CoverType[] coverBuffer = this.coverBuffer;
        this.populateCoverRegion(coverBuffer, chunkX, chunkZ);
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int index = localX + localZ * 16;
                biomes[index] = coverBuffer[index].getBiome(this.getZone(x + localX, z + localZ));
            }
        }
        return biomes;
    }

    private void populateArea(Biome[] biomes, int x, int z, int width, int height) {
        CoverType[] globBuffer = this.coverBuffer;
        int chunkMinX = x >> 4;
        int chunkMinZ = z >> 4;
        int chunkMaxX = (x + width) >> 4;
        int chunkMaxZ = (z + height) >> 4;

        for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                this.initializeRandom(chunkX, chunkZ);
                this.populateCoverRegion(globBuffer, chunkX, chunkZ);

                int minX = Math.max(chunkX << 4, x);
                int maxX = Math.min((chunkX + 1) << 4, x + width);
                int minZ = Math.max(chunkZ << 4, z);
                int maxZ = Math.min((chunkZ + 1) << 4, z + height);
                for (int localZ = minZ; localZ <= maxZ; localZ++) {
                    for (int localX = minX; localX <= maxX; localX++) {
                        int index = (localX - x) + (localZ - z) * 16;
                        biomes[index] = globBuffer[index].getBiome(this.getZone(localX, localZ));
                    }
                }
            }
        }
    }

    private void initializeRandom(int chunkX, int chunkZ) {
        this.random.setSeed(this.world.getSeed());
        long seedX = this.random.nextLong() / 2L * 2L + 1L;
        long seedZ = this.random.nextLong() / 2L * 2L + 1L;
        this.random.setSeed(chunkX * seedX + chunkZ * seedZ ^ this.world.getSeed());
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

    protected abstract void populateCoverRegion(CoverType[] coverBuffer, int chunkX, int chunkZ);

    protected abstract LatitudinalZone getZone(int x, int z);
}
