package net.gegy1000.terrarium.server.world.generator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.composer.ChunkComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.init.Biomes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        private final ImmutableList.Builder<StructureComposer> structureComposers = new ImmutableList.Builder<>();
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

        public Builder withStructureComposer(StructureComposer composer) {
            this.structureComposers.add(composer);
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
            CompositionProcedure compositionProcedure = new CompositionProcedure(this.surfaceComposers.build(), this.structureComposers.build(), this.decorationComposers.build(), this.biomeComposer);
            ICapabilityProvider[] capabilities = this.capabilities.toArray(new ICapabilityProvider[0]);
            return new BasicTerrariumGenerator(compositionProcedure, this.spawnPosition, capabilities);
        }
    }

    private static class CompositionProcedure implements ChunkCompositionProcedure {
        private final ImmutableList<SurfaceComposer> surfaceComposers;
        private final ImmutableList<StructureComposer> structureComposers;
        private final ImmutableList<DecorationComposer> decorationComposers;
        @Nullable
        private final BiomeComposer biomeComposer;

        private CompositionProcedure(ImmutableList<SurfaceComposer> surfaceComposers, ImmutableList<StructureComposer> structureComposers, ImmutableList<DecorationComposer> decorationComposers, BiomeComposer biomeComposer) {
            this.surfaceComposers = surfaceComposers;
            this.structureComposers = structureComposers;
            this.decorationComposers = decorationComposers;
            this.biomeComposer = biomeComposer;
        }

        @Override
        public void composeSurface(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
            for (SurfaceComposer composer : this.surfaceComposers) {
                composer.composeSurface(generator, primer, regionHandler, chunkX, chunkZ);
            }
        }

        @Override
        public void composeDecoration(IChunkGenerator generator, World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
            for (DecorationComposer composer : this.decorationComposers) {
                composer.composeDecoration(generator, world, regionHandler, chunkX, chunkZ);
            }
        }

        @Override
        public Biome[] composeBiomes(RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
            if (this.biomeComposer == null) {
                return ArrayUtils.defaulted(new Biome[16 * 16], Biomes.DEFAULT);
            }
            return this.biomeComposer.composeBiomes(regionHandler, chunkX, chunkZ);
        }

        @Override
        public void composeStructures(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
            for (StructureComposer composer : this.structureComposers) {
                composer.composeStructures(generator, primer, regionHandler, chunkX, chunkZ);
            }
        }

        @Override
        public void populateStructures(World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
            for (StructureComposer composer : this.structureComposers) {
                composer.populateStructures(world, regionHandler, chunkX, chunkZ);
            }
        }

        @Override
        public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
            for (StructureComposer composer : this.structureComposers) {
                boolean inside = composer.isInsideStructure(world, structureName, pos);
                if (inside) {
                    return true;
                }
            }
            return false;
        }

        @Nullable
        @Override
        public BlockPos getNearestStructure(World world, String structureName, BlockPos pos, boolean findUnexplored) {
            for (StructureComposer composer : this.structureComposers) {
                BlockPos nearestStructure = composer.getNearestStructure(world, structureName, pos, findUnexplored);
                if (nearestStructure != null) {
                    return nearestStructure;
                }
            }
            return null;
        }

        @Override
        public Set<RegionComponentType<?>> getSurfaceDependencies() {
            return this.getDependencies(this.surfaceComposers);
        }

        @Override
        public Set<RegionComponentType<?>> getStructureDependencies() {
            return this.getDependencies(this.structureComposers);
        }

        @Override
        public Set<RegionComponentType<?>> getDecorationDependencies() {
            return this.getDependencies(this.decorationComposers);
        }

        @Override
        public Set<RegionComponentType<?>> getBiomeDependencies() {
            if (this.biomeComposer == null) {
                return Collections.emptySet();
            }
            return Sets.newHashSet(this.biomeComposer.getDependencies());
        }

        private Set<RegionComponentType<?>> getDependencies(Collection<? extends ChunkComposer> composers) {
            Set<RegionComponentType<?>> dependencies = new HashSet<>();
            for (ChunkComposer composer : composers) {
                Collections.addAll(dependencies, composer.getDependencies());
            }
            return dependencies;
        }
    }
}
