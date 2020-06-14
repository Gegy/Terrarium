package net.gegy1000.terrarium.server.world.chunk;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.generator.GenericChunkGenerator;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.data.ColumnData;
import net.gegy1000.terrarium.server.world.data.ColumnDataCache;
import net.gegy1000.terrarium.server.world.data.ColumnDataEntry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
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
            ChunkPos columnPos = new ChunkPos(pos.getX(), pos.getZ());

            try (ColumnDataEntry.Handle handle = terrarium.getDataCache().acquireEntry(columnPos)) {
                ColumnData data = handle.join();
                terrarium.getSurfaceComposer().composeSurface(terrarium, data, pos, writer);
                terrarium.getStructureComposer().primeStructures(terrarium, pos, writer);
            }
        });
    }

    @Override
    public void populateChunk(CubicPos pos, ChunkPopulationWriter writer) {
        this.terrarium.get().ifPresent(terrarium -> {
            ColumnDataCache dataCache = terrarium.getDataCache();
            ColumnDataEntry.Handle[] handles = this.acquirePopulationHandles(pos, dataCache);

            terrarium.getStructureComposer().populateStructures(terrarium, pos, writer);
            terrarium.getDecorationComposer().composeDecoration(terrarium, pos, writer);

            for (ColumnDataEntry.Handle handle : handles) {
                handle.release();
            }
        });
    }

    protected ColumnDataEntry.Handle[] acquirePopulationHandles(CubicPos pos, ColumnDataCache dataCache) {
        return new ColumnDataEntry.Handle[] {
                dataCache.acquireEntry(new ChunkPos(pos.getX(), pos.getZ())),
                dataCache.acquireEntry(new ChunkPos(pos.getX() + 1, pos.getZ())),
                dataCache.acquireEntry(new ChunkPos(pos.getX(), pos.getZ() + 1)),
                dataCache.acquireEntry(new ChunkPos(pos.getX() + 1, pos.getZ() + 1)),
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
            terrarium.getStructureComposer().prepareStructures(terrarium, pos);
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
