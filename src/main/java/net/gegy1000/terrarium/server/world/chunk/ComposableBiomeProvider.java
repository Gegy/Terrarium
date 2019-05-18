package net.gegy1000.terrarium.server.world.chunk;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataEntry;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ComposableBiomeProvider extends BiomeProvider {
    private final World world;

    private final Lazy<ColumnDataCache> dataCache;
    private final Lazy<BiomeComposer> biomeComposer;

    private final BiomeCache biomeCache = new BiomeCache(this);

    public ComposableBiomeProvider(World world) {
        this.world = world;

        this.dataCache = new Lazy<>(() -> {
            TerrariumWorldData capability = this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
            if (capability != null) {
                return capability.getDataCache();
            }
            throw new IllegalStateException("Tried to load ColumnDataCache before it was present");
        });

        this.biomeComposer = new Lazy.WorldCap<>(world, TerrariumWorldData::getBiomeComposer);
    }

    @Override
    public List<Biome> getBiomesToSpawnIn() {
        return allowedBiomes;
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
            biomes = new Biome[width * height];
        }
        Arrays.fill(biomes, Biomes.DEFAULT);
        return biomes;
    }

    @Override
    public Biome[] getBiomes(Biome[] biomes, int x, int z, int width, int height, boolean cache) {
        if (biomes == null || biomes.length < width * height) {
            biomes = new Biome[width * height];
        }

        boolean fillsChunk = this.isChunk(x, z, width, height);
        if (cache && fillsChunk) {
            System.arraycopy(this.biomeCache.getCachedBiomes(x, z), 0, biomes, 0, width * height);
        } else {
            this.populateArea(biomes, x, z, width, height);
        }

        return biomes;
    }

    @Override
    public boolean areBiomesViable(int originX, int originZ, int radius, List<Biome> allowed) {
        int minX = originX - radius;
        int minZ = originZ - radius;
        int size = (radius * 2) + 1;

        Biome[] biomes = new Biome[size * size];
        this.populateArea(biomes, minX, minZ, size, size);
        for (Biome biome : biomes) {
            if (!allowed.contains(biome)) {
                return false;
            }
        }

        return true;
    }

    // TODO: Implement properly
    @Override
    public BlockPos findBiomePosition(int originX, int originZ, int radius, List<Biome> allowed, Random random) {
        return new BlockPos(originX, 0, originZ);
    }

    private void populateArea(Biome[] resultBiomes, int x, int z, int width, int height) {
        ColumnDataCache dataCache = this.dataCache.get();

        BiomeComposer biomeComposer = this.biomeComposer.get();

        if (this.isChunk(x, z, width, height)) {
            ChunkPos columnPos = new ChunkPos(x >> 4, z >> 4);
            try (ColumnDataEntry.Handle handle = dataCache.acquireEntry(columnPos)) {
                ColumnData data = handle.join();
                Biome[] biomeBuffer = biomeComposer.composeBiomes(data, columnPos);
                System.arraycopy(biomeBuffer, 0, resultBiomes, 0, biomeBuffer.length);
            }
            return;
        }

        int chunkMinX = x >> 4;
        int chunkMinZ = z >> 4;
        int chunkMaxX = (x + width) >> 4;
        int chunkMaxZ = (z + height) >> 4;

        Collection<ColumnDataEntry.Handle> columnHandles = new ArrayList<>();
        for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
            for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                columnHandles.add(dataCache.acquireEntry(new ChunkPos(chunkX, chunkZ)));
            }
        }

        try {
            for (ColumnDataEntry.Handle handle : columnHandles) {
                ColumnData data = handle.join();
                ChunkPos columnPos = handle.getColumnPos();
                Biome[] biomeBuffer = biomeComposer.composeBiomes(data, columnPos);

                int minColumnX = columnPos.getXStart();
                int minColumnZ = columnPos.getZStart();

                int minX = Math.max(0, x - minColumnX);
                int minZ = Math.max(0, z - minColumnZ);
                int maxX = Math.min(16, (x + width) - minColumnX);
                int maxZ = Math.min(16, (x + height) - minColumnZ);

                for (int localZ = minZ; localZ < maxZ; localZ++) {
                    int resultZ = (localZ + minColumnZ) - z;

                    int localX = minX;
                    int resultX = (localX + minColumnX) - x;

                    int sourceIndex = localX + localZ * 16;
                    int resultIndex = resultX + resultZ * width;

                    System.arraycopy(biomeBuffer, sourceIndex, resultBiomes, resultIndex, maxX - minX);
                }
            }
        } finally {
            columnHandles.forEach(ColumnDataEntry.Handle::release);
        }
    }

    private boolean isChunk(int x, int z, int width, int height) {
        return width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0;
    }

    @Override
    public void cleanupCache() {
        this.biomeCache.cleanupCache();
    }
}
