package net.gegy1000.terrarium.server.world.chunk;

import dev.gegy.gengen.api.CubicPos;
import dev.gegy.gengen.api.generator.GenericChunkGenerator;
import dev.gegy.gengen.api.writer.ChunkPopulationWriter;
import dev.gegy.gengen.api.writer.ChunkPrimeWriter;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.util.Profiler;
import net.gegy1000.terrarium.server.util.ThreadedProfiler;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.ColumnDataEntry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComposableChunkGenerator implements GenericChunkGenerator {
    protected final World world;

    protected final Lazy<Optional<TerrariumWorld>> terrarium;

    public ComposableChunkGenerator(World world) {
        this.world = world;
        this.terrarium = Lazy.ofCapability(world, TerrariumCapabilities.world());
    }

    @Override
    public void primeChunk(CubicPos pos, ChunkPrimeWriter writer) {
        this.terrarium.get().ifPresent(terrarium -> {
            Profiler profiler = ThreadedProfiler.get();

            try (
                    Profiler.Handle generate = profiler.push("generate");
                    Profiler.Handle prime = profiler.push("prime");
            ) {
                try (ColumnDataEntry.Handle handle = terrarium.getDataCache().acquireEntry(pos.getX(), pos.getZ())) {
                    ColumnData data = handle.join();

                    try (Profiler.Handle surface = profiler.push("surface")) {
                        terrarium.getSurfaceComposer().composeSurface(terrarium, data, pos, writer);
                    }

                    try (Profiler.Handle structures = profiler.push("structures")) {
                        terrarium.getStructureComposer().primeStructures(terrarium, pos, writer);
                    }
                }
            }
        });
    }

    @Override
    public void populateChunk(CubicPos pos, ChunkPopulationWriter writer) {
        this.terrarium.get().ifPresent(terrarium -> {
            Profiler profiler = ThreadedProfiler.get();

            try (
                    Profiler.Handle generate = profiler.push("generate");
                    Profiler.Handle populate = profiler.push("populate");
            ) {
                ColumnDataCache dataCache = terrarium.getDataCache();
                ColumnDataEntry.Handle[] handles = this.acquirePopulationHandles(pos, dataCache);

                try (Profiler.Handle structures = profiler.push("structures")) {
                    terrarium.getStructureComposer().populateStructures(terrarium, pos, writer);
                }

                try (Profiler.Handle decoration = profiler.push("decoration")) {
                    terrarium.getDecorationComposer().composeDecoration(terrarium, pos, writer);
                }

                for (ColumnDataEntry.Handle handle : handles) {
                    handle.release();
                }
            }
        });
    }

    protected ColumnDataEntry.Handle[] acquirePopulationHandles(CubicPos pos, ColumnDataCache dataCache) {
        return new ColumnDataEntry.Handle[] {
                dataCache.acquireEntry(pos.getX(), pos.getZ()),
                dataCache.acquireEntry(pos.getX() + 1, pos.getZ()),
                dataCache.acquireEntry(pos.getX(), pos.getZ() + 1),
                dataCache.acquireEntry(pos.getX() + 1, pos.getZ() + 1),
        };
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        return this.terrarium.get()
                .map(terrarium -> terrarium.getStructureComposer().getPossibleCreatures(terrarium, this.world, type, pos))
                .orElseGet(() -> this.world.getBiome(pos).getSpawnableList(type));
    }

    @Override
    public void prepareStructures(CubicPos pos) {
        this.terrarium.get().ifPresent(terrarium -> {
            Profiler profiler = ThreadedProfiler.get();

            try (
                    Profiler.Handle generate = profiler.push("generate");
                    Profiler.Handle prepare = profiler.push("prepare_structures");
            ) {
                terrarium.getStructureComposer().prepareStructures(terrarium, pos);
            }
        });
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        return this.terrarium.get().map(terrarium -> {
            StructureComposer composer = terrarium.getStructureComposer();
            return composer.getClosestStructure(terrarium, this.world, name, pos, findUnexplored);
        }).orElse(null);
    }

    @Override
    public boolean isInsideStructure(String name, BlockPos pos) {
        Optional<TerrariumWorld> terrariumOption = this.terrarium.get();
        if (terrariumOption.isPresent()) {
            TerrariumWorld terrarium = terrariumOption.get();
            StructureComposer composer = terrarium.getStructureComposer();
            return composer.isInsideStructure(terrarium, this.world, name, pos);
        }
        return false;
    }
}
