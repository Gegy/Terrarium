package dev.gegy.terrarium.world.generator.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.gegy.terrarium.backend.GeoChunk;
import dev.gegy.terrarium.world.GeoProvider;
import dev.gegy.terrarium.world.GeoProviderHolder;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.Arrays;

public abstract class GeoBiomeSource extends BiomeSource {
    public static final Codec<GeoBiomeSource> CODEC = BiomeSource.CODEC.comapFlatMap(
            biomeSource -> {
                if (biomeSource instanceof final GeoBiomeSource geoBiomeSource) {
                    return DataResult.success(geoBiomeSource);
                } else {
                    return DataResult.error(() -> biomeSource + " was not a GeoBiomeSource");
                }
            },
            geoBiomeSource -> geoBiomeSource
    );

    private final ThreadLocal<ResolverCache> resolverCache = ThreadLocal.withInitial(ResolverCache::new);

    // This is intended only as a fallback path, and has very poor performance characteristics.
    // Where possible, the GeoChunk-accepting variants should be preferred.
    @Override
    @Deprecated
    public final Holder<Biome> getNoiseBiome(final int x, final int y, final int z, final Climate.Sampler sampler) {
        final FlatChunkResolver resolver = resolverCache.get().get(QuartPos.toSection(x), QuartPos.toSection(z), sampler);
        final int relativeX = SectionPos.sectionRelative(QuartPos.toBlock(x));
        final int relativeZ = SectionPos.sectionRelative(QuartPos.toBlock(z));
        return resolver.get(relativeX, relativeZ);
    }

    public abstract FlatChunkResolver chunkResolver(GeoChunk geoChunk);

    public interface FlatChunkResolver {
        int CHUNK_SIZE = SectionPos.SECTION_SIZE >> QuartPos.BITS;

        Holder<Biome> get(int x, int z);

        default BiomeResolver toFullBiomeResolver() {
            final Holder<Biome>[] biomes = new Holder[CHUNK_SIZE * CHUNK_SIZE];
            for (int z = 0; z < CHUNK_SIZE; z++) {
                for (int x = 0; x < CHUNK_SIZE; x++) {
                    final int relativeX = SectionPos.sectionRelative(QuartPos.toBlock(x));
                    final int relativeZ = SectionPos.sectionRelative(QuartPos.toBlock(z));
                    biomes[x + z * CHUNK_SIZE] = get(relativeX, relativeZ);
                }
            }
            return (x, y, z, sampler) -> biomes[QuartPos.quartLocal(x) + QuartPos.quartLocal(z) * CHUNK_SIZE];
        }
    }

    private class ResolverCache {
        private static final int SIZE = 5;

        private final long[] keys = Util.make(new long[SIZE], keys -> Arrays.fill(keys, ChunkPos.INVALID_CHUNK_POS));
        private final FlatChunkResolver[] values = new FlatChunkResolver[SIZE];

        public FlatChunkResolver get(final int chunkX, final int chunkZ, final Climate.Sampler sampler) {
            final long[] keys = this.keys;
            final FlatChunkResolver[] values = this.values;
            final long key = ChunkPos.asLong(chunkX, chunkZ);
            for (int i = 0; i < keys.length; i++) {
                if (keys[i] == key) {
                    return values[i];
                }
            }
            for (int i = SIZE - 1; i > 0; i--) {
                keys[i] = keys[i - 1];
                values[i] = values[i - 1];
            }
            final FlatChunkResolver resolver = compute(chunkX, chunkZ, sampler);
            keys[0] = key;
            values[0] = resolver;
            return resolver;
        }

        private FlatChunkResolver compute(final int chunkX, final int chunkZ, final Climate.Sampler sampler) {
            final GeoProvider geoProvider = GeoProviderHolder.get(sampler);
            final GeoChunk geoChunk;
            if (geoProvider != null) {
                geoChunk = geoProvider.getOrLoadSync(new ChunkPos(chunkX, chunkZ));
            } else {
                geoChunk = GeoChunk.EMPTY;
            }
            return chunkResolver(geoChunk);
        }
    }
}
