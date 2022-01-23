package net.gegy1000.terrarium.server.world.chunk;

import net.gegy1000.justnow.executor.CurrentThreadExecutor;
import net.gegy1000.justnow.future.Future;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.data.DataSample;
import net.gegy1000.terrarium.server.world.data.DataView;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeCache;
import net.minecraft.world.biome.BiomeProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class ComposableBiomeProvider extends BiomeProvider {
    private final Lazy<Optional<TerrariumWorld>> terrarium;

    private final BiomeCache biomeCache = new BiomeCache(this);

    private DataView viableCacheView;
    private boolean viableCacheResult;

    public ComposableBiomeProvider(World world) {
        this.terrarium = Lazy.ofCapability(world, TerrariumCapabilities.world());
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
            this.populateArea(biomes, DataView.of(x, z, width, height));
        }

        return biomes;
    }

    @Override
    public boolean areBiomesViable(int originX, int originZ, int radius, List<Biome> allowed) {
        if (radius == 0) {
            Biome biome = this.biomeCache.getBiome(originX, originZ, null);
            return allowed.contains(biome);
        }

        int minX = originX - radius;
        int minZ = originZ - radius;
        int size = (radius * 2) + 1;
        DataView view = DataView.ofSquare(minX, minZ, size);

        if (view.equals(this.viableCacheView)) {
            return this.viableCacheResult;
        }

        boolean result = true;

        Biome[] biomes = new Biome[size * size];
        this.populateArea(biomes, view);
        for (Biome biome : biomes) {
            if (!allowed.contains(biome)) {
                result = false;
                break;
            }
        }

        this.viableCacheView = view;
        this.viableCacheResult = result;

        return result;
    }

    @Override
    public BlockPos findBiomePosition(int originX, int originZ, int radius, List<Biome> allowed, Random random) {
        if (radius == 0) return null;

        int minX = originX - radius;
        int minZ = originZ - radius;
        int size = (radius * 2) + 1;
        DataView view = DataView.of(minX, minZ, size, size);

        Biome[] biomes = new Biome[size * size];
        this.populateArea(biomes, view);

        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                if (allowed.contains(biomes[x + z * size])) {
                    return new BlockPos(x + minX, 0, z + minZ);
                }
            }
        }

        return null;
    }

    private void populateArea(Biome[] resultBiomes, DataView view) {
        Optional<TerrariumWorld> terrariumOption = this.terrarium.get();
        if (!terrariumOption.isPresent()) {
            Arrays.fill(resultBiomes, Biomes.PLAINS);
            return;
        }

        TerrariumWorld terrarium = terrariumOption.get();

        Profiler profiler = ThreadedProfiler.get();
        try (
                Profiler.Handle biomes = profiler.push("biomes");
                Profiler.Handle sampleArea = profiler.push("sample_area")
        ) {
            BiomeComposer biomeComposer = terrarium.getBiomeComposer();

            if (this.isChunk(view.minX(), view.minY(), view.width(), view.height())) {
                DataSample data = terrarium.getDataCache().joinData(view.minX() >> 4, view.minY() >> 4);
                biomeComposer.composeBiomes(resultBiomes, terrarium, data, view);
                return;
            }

            Future<DataSample> future = terrarium.getDataGenerator().generate(view);
            DataSample data = CurrentThreadExecutor.blockOn(future);

            biomeComposer.composeBiomes(resultBiomes, terrarium, data, view);
        }
    }

    private boolean isChunk(int x, int y, int width, int height) {
        return width == 16 && height == 16 && (x & 15) == 0 && (y & 15) == 0;
    }

    @Override
    public void cleanupCache() {
        this.biomeCache.cleanupCache();
    }
}
