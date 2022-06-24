package dev.gegy.terrarium.world.generator.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.world.chunk.GeoChunkHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class EarthChunkGenerator extends ChunkGenerator implements GeoChunkGenerator {
    public static final Codec<EarthChunkGenerator> CODEC = RecordCodecBuilder.create(i -> i.group(
        BiomeSource.CODEC.fieldOf("biome_source").forGetter(c -> c.biomeSource)
    ).apply(i, EarthChunkGenerator::new));

    public EarthChunkGenerator(final BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(final WorldGenRegion region, final long seed, final RandomState randomState, final BiomeManager biomes, final StructureManager structures, final ChunkAccess chunk, final GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(final WorldGenRegion region, final StructureManager structures, final RandomState randomState, final ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(final WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return 0;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(final Executor executor, final Blender blender, final RandomState randomState, final StructureManager structures, final ChunkAccess chunk) {
        final GeoChunk geoChunk = GeoChunkHolder.get(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(final int x, final int z, final Heightmap.Types heightmap, final LevelHeightAccessor levelHeight, final RandomState randomState) {
        return 0;
    }

    @Override
    public NoiseColumn getBaseColumn(final int x, final int z, final LevelHeightAccessor levelHeight, final RandomState randomState) {
        return new NoiseColumn(levelHeight.getMinBuildHeight(), new BlockState[]{Blocks.STONE.defaultBlockState()});
    }

    @Override
    public void addDebugScreenInfo(final List<String> lines, final RandomState randomState, final BlockPos pos) {
    }

    @Override
    public CompletableFuture<GeoChunk> loadGeoChunk(final ChunkPos pos) {
        return CompletableFuture.completedFuture(new GeoChunk());
    }
}
