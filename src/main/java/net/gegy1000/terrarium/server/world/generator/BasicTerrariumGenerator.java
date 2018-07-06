package net.gegy1000.terrarium.server.world.generator;

import com.google.common.collect.ImmutableList;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.region.GenerationRegionHandler;
import net.minecraft.init.Biomes;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BasicTerrariumGenerator implements TerrariumGenerator {
    private final ChunkCompositionProcedure compositionProcedure;
    private final Coordinate spawnPosition;
    private final ICapabilityProvider[] capabilities;

    private BasicTerrariumGenerator(ChunkCompositionProcedure compositionProcedure, Coordinate spawnPosition, ICapabilityProvider[] capabilities) {
        this.compositionProcedure = compositionProcedure;
        this.spawnPosition = spawnPosition;
        this.capabilities = capabilities;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ChunkCompositionProcedure getCompositionProcedure() {
        return this.compositionProcedure;
    }

    @Override
    public Coordinate getSpawnPosition() {
        return this.spawnPosition;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        for (ICapabilityProvider provider : this.capabilities) {
            if (provider.hasCapability(capability, facing)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        for (ICapabilityProvider provider : this.capabilities) {
            T provided = provider.getCapability(capability, facing);
            if (provided != null) {
                return provided;
            }
        }
        return null;
    }

    public static class Builder {
        private final ImmutableList.Builder<SurfaceComposer> surfaceComposers = new ImmutableList.Builder<>();
        private final ImmutableList.Builder<DecorationComposer> decorationComposers = new ImmutableList.Builder<>();
        @Nullable
        private BiomeComposer biomeComposer;

        private final List<ICapabilityProvider> capabilities = new ArrayList<>();

        private Coordinate spawnPosition;

        private Builder() {
        }

        public Builder withSurfaceComposer(SurfaceComposer composer) {
            this.surfaceComposers.add(composer);
            return this;
        }

        public Builder withDecorationComposer(DecorationComposer composer) {
            this.decorationComposers.add(composer);
            return this;
        }

        public Builder withBiomeComposer(BiomeComposer composer) {
            this.biomeComposer = composer;
            return this;
        }

        public Builder withSpawnPosition(Coordinate coordinate) {
            this.spawnPosition = coordinate;
            return this;
        }

        public Builder withCapability(ICapabilityProvider provider) {
            this.capabilities.add(provider);
            return this;
        }

        public BasicTerrariumGenerator build() {
            CompositionProcedure compositionProcedure = new CompositionProcedure(this.surfaceComposers.build(), this.decorationComposers.build(), this.biomeComposer);
            ICapabilityProvider[] capabilities = this.capabilities.toArray(new ICapabilityProvider[0]);
            return new BasicTerrariumGenerator(compositionProcedure, this.spawnPosition, capabilities);
        }
    }

    private static class CompositionProcedure implements ChunkCompositionProcedure {
        private final ImmutableList<SurfaceComposer> surfaceComposers;
        private final ImmutableList<DecorationComposer> decorationComposers;
        @Nullable
        private final BiomeComposer biomeComposer;

        private CompositionProcedure(ImmutableList<SurfaceComposer> surfaceComposers, ImmutableList<DecorationComposer> decorationComposers, BiomeComposer biomeComposer) {
            this.surfaceComposers = surfaceComposers;
            this.decorationComposers = decorationComposers;
            this.biomeComposer = biomeComposer;
        }

        @Override
        public void composeSurface(ChunkPrimer primer, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
            for (SurfaceComposer composer : this.surfaceComposers) {
                composer.composeSurface(primer, regionHandler, chunkX, chunkZ);
            }
        }

        @Override
        public void composeDecoration(World world, GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
            for (DecorationComposer composer : this.decorationComposers) {
                composer.composeDecoration(world, regionHandler, chunkX, chunkZ);
            }
        }

        @Override
        public Biome[] composeBiomes(GenerationRegionHandler regionHandler, int chunkX, int chunkZ) {
            if (this.biomeComposer == null) {
                return ArrayUtils.defaulted(new Biome[16 * 16], Biomes.DEFAULT);
            }
            return this.biomeComposer.composeBiomes(regionHandler, chunkX, chunkZ);
        }
    }
}
