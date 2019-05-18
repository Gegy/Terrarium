package net.gegy1000.terrarium.server.world.generator;

import com.google.common.collect.ImmutableList;
import net.gegy1000.cubicglue.api.ChunkPopulationWriter;
import net.gegy1000.cubicglue.api.ChunkPrimeWriter;
import net.gegy1000.cubicglue.util.CubicPos;
import net.gegy1000.terrarium.server.util.ArrayUtils;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnData;
import net.gegy1000.terrarium.server.world.pipeline.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.pipeline.composer.biome.BiomeComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.pipeline.composer.surface.SurfaceComposer;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;

public class BasicTerrariumGenerator implements TerrariumGenerator {
    private final ChunkCompositionProcedure compositionProcedure;
    private final Coordinate spawnPosition;

    private BasicTerrariumGenerator(ChunkCompositionProcedure compositionProcedure, Coordinate spawnPosition) {
        this.compositionProcedure = compositionProcedure;
        this.spawnPosition = spawnPosition;
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

    public static class Builder {
        private final ImmutableList.Builder<SurfaceComposer> surfaceComposers = new ImmutableList.Builder<>();
        private final ImmutableList.Builder<StructureComposer> structureComposers = new ImmutableList.Builder<>();
        private final ImmutableList.Builder<DecorationComposer> decorationComposers = new ImmutableList.Builder<>();
        @Nullable
        private BiomeComposer biomeComposer;

        private Coordinate spawnPosition;

        private Builder() {
        }

        public Builder addSurfaceComposer(SurfaceComposer composer) {
            this.surfaceComposers.add(composer);
            return this;
        }

        public Builder addStructureComposer(StructureComposer composer) {
            this.structureComposers.add(composer);
            return this;
        }

        public Builder addDecorationComposer(DecorationComposer composer) {
            this.decorationComposers.add(composer);
            return this;
        }

        public Builder setBiomeComposer(BiomeComposer composer) {
            this.biomeComposer = composer;
            return this;
        }

        public Builder setSpawnPosition(Coordinate coordinate) {
            this.spawnPosition = coordinate;
            return this;
        }

        public BasicTerrariumGenerator build() {
            CompositionProcedure compositionProcedure = new CompositionProcedure(this.surfaceComposers.build(), this.structureComposers.build(), this.decorationComposers.build(), this.biomeComposer);
            return new BasicTerrariumGenerator(compositionProcedure, this.spawnPosition);
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
        public void composeSurface(ColumnData data, CubicPos pos, ChunkPrimeWriter writer) {
            for (SurfaceComposer composer : this.surfaceComposers) {
                composer.composeSurface(data, pos, writer);
            }
        }

        @Override
        public void composeDecoration(ColumnDataCache dataCache, CubicPos pos, ChunkPopulationWriter writer) {
            for (DecorationComposer composer : this.decorationComposers) {
                composer.composeDecoration(dataCache, pos, writer);
            }
        }

        @Override
        public Biome[] composeBiomes(ColumnData data, ChunkPos columnPos) {
            if (this.biomeComposer == null) {
                return ArrayUtils.fill(new Biome[16 * 16], Biomes.DEFAULT);
            }
            return this.biomeComposer.composeBiomes(data, columnPos);
        }

        @Override
        public void composeStructures(IChunkGenerator generator, ChunkPrimer primer, ColumnDataCache dataCache, int chunkX, int chunkZ) {
            for (StructureComposer composer : this.structureComposers) {
                composer.composeStructures(generator, primer, dataCache, chunkX, chunkZ);
            }
        }

        @Override
        public void populateStructures(World world, ColumnDataCache dataCache, int chunkX, int chunkZ) {
            for (StructureComposer composer : this.structureComposers) {
                composer.populateStructures(world, dataCache, chunkX, chunkZ);
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
    }
}
