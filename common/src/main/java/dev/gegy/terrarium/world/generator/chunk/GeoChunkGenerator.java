package dev.gegy.terrarium.world.generator.chunk;

import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.world.GeoProvider;
import dev.gegy.terrarium.world.chunk.GeoChunkHolder;
import dev.gegy.terrarium.world.generator.biome.GeoBiomeSource;
import net.minecraft.Util;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.concurrent.CompletableFuture;

public abstract class GeoChunkGenerator extends ChunkGenerator {
    public GeoChunkGenerator(final GeoBiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    public GeoBiomeSource getBiomeSource() {
        return (GeoBiomeSource) super.getBiomeSource();
    }

    public abstract GeoProvider createGeoProvider();

    protected final GeoChunk getGeoChunk(final ChunkAccess chunk) {
        return GeoChunkHolder.get(chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(final RandomState randomState, final Blender blender, final StructureManager structureManager, final ChunkAccess chunkAccess) {
        if (biomeSource instanceof final GeoBiomeSource geoBiomeSource) {
            final GeoChunk geoChunk = getGeoChunk(chunkAccess);
            return CompletableFuture.supplyAsync(() -> {
                final BiomeResolver resolver = geoBiomeSource.chunkResolver(geoChunk).toFullBiomeResolver();
                chunkAccess.fillBiomesFromNoise(resolver, randomState.sampler());
                return chunkAccess;
            }, Util.backgroundExecutor().forName("init_biomes"));
        }
        return super.createBiomes(randomState, blender, structureManager, chunkAccess);
    }
}
