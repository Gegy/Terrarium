package net.gegy1000.terrarium.server.world.chunk;

import com.google.common.collect.Sets;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.feature.StructureFeature;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ComposableBiomeSource<C extends TerrariumGeneratorConfig> extends BiomeSource {
    private final RegionGenerationHandler regionHandler;
    private final ChunkCompositionProcedure<C> compositionProcedure;

    public ComposableBiomeSource(C config) {
        this.regionHandler = config.getRegionHandler();
        this.compositionProcedure = config.getCompositionProcedure();
    }

    @Override
    public Biome method_16359(int x, int z) {
        this.regionHandler.prepareChunk(x, z, this.compositionProcedure.getBiomeDependencies());
        Biome[] biomes = this.compositionProcedure.composeBiomes(this.regionHandler, x >> 4, z >> 4);
        return biomes[(x & 15) + (z & 15) * 16];
    }

    @Override
    public Biome method_16360(int var1, int var2) {
        return Biomes.DEFAULT;
    }

    @Override
    public Biome[] method_8760(int x, int z, int width, int height, boolean flag) {
        Biome[] biomes = new Biome[width * height];
        this.populateArea(biomes, x, z, width, height);
        return biomes;
    }

    @Override
    public Set<Biome> method_8763(int originX, int originZ, int radius) {
        int minX = originX - radius >> 2;
        int minZ = originZ - radius >> 2;
        int maxX = originX + radius >> 2;
        int maxZ = originZ + radius >> 2;
        int width = maxX - minX + 1;
        int height = maxZ - minZ + 1;

        Set<Biome> biomes = Sets.newHashSet();
        Collections.addAll(biomes, this.method_8760(minX, minZ, width, height, false));
        return biomes;
    }

    // TODO: implement properly
    @Override
    @Nullable
    public BlockPos method_8762(int originX, int originZ, int radius, List<Biome> allowed, Random random) {
        return new BlockPos(originX, 0, originZ);
    }

    @Override
    public boolean hasStructureFeature(StructureFeature<?> feature) {
        return this.STRUCTURE_FEATURES.computeIfAbsent(feature, f -> {
            for (Biome biome : Registry.BIOME) {
                if (biome.hasStructureFeature(f)) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public Set<BlockState> getTopMaterials() {
        if (this.topMaterials.isEmpty()) {
            for (Biome biome : Registry.BIOME) {
                this.topMaterials.add(biome.getSurfaceConfig().getTopMaterial());
            }
        }
        return this.topMaterials;
    }

    private void populateArea(Biome[] biomes, int x, int z, int width, int height) {
        if (this.isChunkGeneration(x, z, width, height)) {
            this.regionHandler.prepareChunk(x, z, this.compositionProcedure.getBiomeDependencies());
            Biome[] biomeBuffer = this.compositionProcedure.composeBiomes(this.regionHandler, x >> 4, z >> 4);
            System.arraycopy(biomeBuffer, 0, biomes, 0, biomeBuffer.length);
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

                this.regionHandler.prepareChunk(minChunkX, minChunkZ, this.compositionProcedure.getBiomeDependencies());
                Biome[] biomeBuffer = this.compositionProcedure.composeBiomes(this.regionHandler, chunkX, chunkZ);

                for (int localZ = minZ; localZ < maxZ; localZ++) {
                    int srcPos = (localZ - minChunkZ) * 16;
                    int destPos = (localZ - z) * width;
                    System.arraycopy(biomeBuffer, srcPos, biomes, destPos, maxX - minX);
                }
            }
        }
    }

    private boolean isChunkGeneration(int x, int z, int width, int height) {
        return width == 16 && height == 16 && (x & 15) == 0 && (z & 15) == 0;
    }
}
