package net.gegy1000.terrarium.server.world.pipeline.composer;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Sets;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorConfig;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.ChunkComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.chunk.TerrainNoiseComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.class_2919;
import net.minecraft.class_3233;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BasicCompositionProcedure<C extends TerrariumGeneratorConfig> implements ChunkCompositionProcedure<C> {
    private final ImmutableMultimap<ChunkStatus, ChunkComposer<C>> composers;
    private final ImmutableMultimap<ChunkStatus, DecorationComposer<C>> decorationComposers;

    @Nullable
    private final TerrainNoiseComposer<C> noiseComposer;

    @Nullable
    private final BiomeComposer biomeComposer;

    private final ImmutableMultimap<ChunkStatus, RegionComponentType<?>> composerDependencies;
    private final Set<RegionComponentType<?>> biomeDependencies;

    private final class_2919 random = new class_2919();

    private BasicCompositionProcedure(
            ImmutableMultimap<ChunkStatus, ChunkComposer<C>> composers,
            ImmutableMultimap<ChunkStatus, DecorationComposer<C>> decorationComposers,
            TerrainNoiseComposer<C> noiseComposer,
            BiomeComposer biomeComposer
    ) {
        this.composers = composers;
        this.decorationComposers = decorationComposers;

        this.noiseComposer = noiseComposer;
        this.biomeComposer = biomeComposer;

        this.composerDependencies = this.buildDependencies();

        if (this.biomeComposer != null) {
            this.biomeDependencies = Sets.newHashSet(this.biomeComposer.getDependencies());
        } else {
            this.biomeDependencies = Collections.emptySet();
        }
    }

    private ImmutableMultimap<ChunkStatus, RegionComponentType<?>> buildDependencies() {
        ImmutableMultimap.Builder<ChunkStatus, RegionComponentType<?>> dependenciesBuilder = ImmutableMultimap.builder();
        for (Map.Entry<ChunkStatus, ChunkComposer<C>> entry : this.composers.entries()) {
            ChunkStatus state = entry.getKey();
            RegionComponentType<?>[] dependencies = entry.getValue().getDependencies();
            dependenciesBuilder.putAll(state, dependencies);
        }
        for (Map.Entry<ChunkStatus, DecorationComposer<C>> entry : this.decorationComposers.entries()) {
            ChunkStatus state = entry.getKey();
            RegionComponentType<?>[] dependencies = entry.getValue().getDependencies();
            dependenciesBuilder.putAll(state, dependencies);
        }

        return dependenciesBuilder.build();
    }

    public static <C extends TerrariumGeneratorConfig> Builder<C> builder() {
        return new Builder<>();
    }

    @Override
    public void compose(ChunkStatus state, ChunkGenerator<C> generator, Chunk chunk, RegionGenerationHandler regionHandler) {
        ChunkPos pos = chunk.getPos();

        int index = 0;
        Collection<ChunkComposer<C>> composers = this.composers.get(state);
        for (ChunkComposer<C> composer : composers) {
            long seed = generator.getSeed() + index++ * 31;
            this.random.method_12661(seed, pos.getXStart(), pos.getZStart());
            composer.compose(generator, chunk, this.random, regionHandler);
        }
    }

    @Override
    public void composeDecoration(ChunkStatus state, ChunkGenerator<C> generator, class_3233 region, RegionGenerationHandler regionHandler) {
        int chunkX = region.method_14336();
        int chunkZ = region.method_14339();

        int index = 0;
        Collection<DecorationComposer<C>> composers = this.decorationComposers.get(state);
        for (DecorationComposer<C> composer : composers) {
            long seed = generator.getSeed() + index++ * 31;
            this.random.method_12661(seed, chunkX << 4, chunkZ << 4);
            composer.compose(generator, region, this.random, regionHandler);
        }
    }

    @Override
    public int sampleHeight(RegionGenerationHandler regionHandler, int x, int z) {
        if (this.noiseComposer != null) {
            return this.noiseComposer.sampleHeight(regionHandler, x, z);
        }
        return 0;
    }

    @Override
    public Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        if (this.biomeComposer == null) {
            return ArrayUtils.defaulted(new Biome[16 * 16], Biomes.DEFAULT);
        }
        return this.biomeComposer.composeBiomes(regionHandler, chunkX, chunkZ);
    }

    @Override
    public Collection<RegionComponentType<?>> getDependencies(ChunkStatus state) {
        return this.composerDependencies.get(state);
    }

    @Override
    public Collection<RegionComponentType<?>> getBiomeDependencies() {
        return this.biomeDependencies;
    }

    public static class Builder<C extends TerrariumGeneratorConfig> {
        private final ImmutableMultimap.Builder<ChunkStatus, ChunkComposer<C>> composers = ImmutableMultimap.builder();
        private final ImmutableMultimap.Builder<ChunkStatus, DecorationComposer<C>> decorationComposers = ImmutableMultimap.builder();

        private TerrainNoiseComposer<C> noiseComposer;
        private BiomeComposer biomeComposer;

        private Builder() {
        }

        public Builder<C> withComposer(ChunkStatus state, ChunkComposer<C> composer) {
            this.composers.put(state, composer);
            return this;
        }

        public Builder<C> withDecorationComposer(ChunkStatus state, DecorationComposer<C> composer) {
            this.decorationComposers.put(state, composer);
            return this;
        }

        public Builder<C> withNoiseComposer(TerrainNoiseComposer<C> composer) {
            this.noiseComposer = composer;
            return this.withComposer(ChunkStatus.NOISE, composer);
        }

        public Builder<C> withBiomeComposer(BiomeComposer composer) {
            this.biomeComposer = composer;
            return this;
        }

        public BasicCompositionProcedure<C> build() {
            return new BasicCompositionProcedure<>(this.composers.build(), this.decorationComposers.build(), this.noiseComposer, this.biomeComposer);
        }
    }
}
