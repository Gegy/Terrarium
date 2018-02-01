package net.gegy1000.terrarium.server.world.generator;

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

public abstract class CoveredBiomeProvider extends BiomeProvider {
    protected final World world;

    protected final BiomeCache biomeCache = new BiomeCache(this);
    private final List<Biome> spawnBiomes = new ArrayList<>(allowedBiomes);

    private final Biome[] chunkBiomeBuffer = ArrayUtils.defaulted(new Biome[256], Biomes.DEFAULT);

    public CoveredBiomeProvider(World world) {
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
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[256];
        }
        Arrays.fill(biomes, Biomes.DEFAULT);
        return biomes;
    }

    @Override
    public Biome[] getBiomes(Biome[] biomes, int x, int z, int width, int height, boolean cache) {
        IntCache.resetIntCache();
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }

        boolean fillsChunk = this.isChunkGeneration(x, z, width, height);
        if (cache && fillsChunk) {
            System.arraycopy(this.biomeCache.getCachedBiomes(x, z), 0, biomes, 0, width * height);
        } else {
            this.populateArea(biomes, x, z, width, height);
        }
        return biomes;
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

    protected final void populateArea(Biome[] biomes, int x, int z, int width, int height) {
        if (this.isChunkGeneration(x, z, width, height)) {
            this.populateChunk(biomes, x, z);
            return;
        }

        int chunkMinX = x >> 4;
        int chunkMinZ = z >> 4;
        int chunkMaxX = (x + width) >> 4;
        int chunkMaxZ = (z + height) >> 4;

        for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                int minChunkX = chunkX << 4;
                int minChunkZ = chunkZ << 4;

                int minX = Math.max(minChunkX, x);
                int maxX = Math.min((chunkX + 1) << 4, x + width);
                int minZ = Math.max(minChunkZ, z);
                int maxZ = Math.min((chunkZ + 1) << 4, z + height);

                this.populateChunk(this.chunkBiomeBuffer, minChunkX, minChunkZ);

                for (int localZ = minZ; localZ < maxZ; localZ++) {
                    int srcPos = (localZ - z) * 16;
                    int destPos = minX + localZ * width;
                    System.arraycopy(this.chunkBiomeBuffer, srcPos, biomes, destPos, maxX - minX);
                }
            }
        }
    }

    private boolean isChunkGeneration(int x, int z, int width, int height) {
        return width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0;
    }

    protected abstract void populateChunk(Biome[] biomes, int x, int z);

    @Override
    public void cleanupCache() {
        this.biomeCache.cleanupCache();
    }
}
