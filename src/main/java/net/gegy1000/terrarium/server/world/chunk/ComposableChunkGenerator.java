package net.gegy1000.terrarium.server.world.chunk;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.generator.GenericChunkGenerator;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.composer.decoration.DecorationComposer;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.gegy1000.terrarium.server.world.composer.surface.SurfaceComposer;
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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComposableChunkGenerator implements GenericChunkGenerator {
    private final World world;

    private final Lazy<SurfaceComposer> surfaceComposer;
    private final Lazy<DecorationComposer> decorationComposer;
    private final Lazy<StructureComposer> structureComposer;

    private final Lazy<ColumnDataCache> dataCache;

    private final ColumnDataEntry.Handle[] populationHandles = new ColumnDataEntry.Handle[4];

    public ComposableChunkGenerator(World world) {
        this.world = world;

        this.surfaceComposer = Lazy.worldCap(world, TerrariumWorld::getSurfaceComposer);
        this.decorationComposer = Lazy.worldCap(world, TerrariumWorld::getDecorationComposer);
        this.structureComposer = Lazy.worldCap(world, TerrariumWorld::getStructureComposer);

        this.dataCache = Lazy.worldCap(world, TerrariumWorld::getDataCache);
    }

    @Override
    public void primeChunk(CubicPos pos, ChunkPrimeWriter writer) {
        ColumnDataCache dataCache = this.dataCache.get();

        ChunkPos columnPos = new ChunkPos(pos.getX(), pos.getZ());
        try (ColumnDataEntry.Handle handle = dataCache.acquireEntry(columnPos)) {
            ColumnData data = handle.join();
            this.surfaceComposer.get().composeSurface(data, pos, writer);
        }

        this.structureComposer.get().primeStructures(pos, writer);
    }

    @Override
    public void populateChunk(CubicPos pos, ChunkPopulationWriter writer) {
        ColumnDataCache dataCache = this.dataCache.get();

        ColumnDataEntry.Handle[] handles = this.acquirePopulationHandles(pos, dataCache);

        this.decorationComposer.get().composeDecoration(dataCache, pos, writer);
        this.structureComposer.get().populateStructures(pos, writer);

        for (ColumnDataEntry.Handle handle : handles) {
            handle.release();
        }
    }

    private ColumnDataEntry.Handle[] acquirePopulationHandles(CubicPos pos, ColumnDataCache dataCache) {
        this.populationHandles[0] = dataCache.acquireEntry(new ChunkPos(pos.getX(), pos.getZ()));
        this.populationHandles[1] = dataCache.acquireEntry(new ChunkPos(pos.getX() + 1, pos.getZ()));
        this.populationHandles[2] = dataCache.acquireEntry(new ChunkPos(pos.getX(), pos.getZ() + 1));
        this.populationHandles[3] = dataCache.acquireEntry(new ChunkPos(pos.getX() + 1, pos.getZ() + 1));
        return this.populationHandles;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        return this.world.getBiome(pos).getSpawnableList(type);
    }

    @Override
    public void prepareStructures(CubicPos pos) {
        this.structureComposer.get().prepareStructures(pos);
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        return this.structureComposer.get().getClosestStructure(this.world, name, pos, findUnexplored);
    }

    @Override
    public boolean isInsideStructure(String name, BlockPos pos) {
        return this.structureComposer.get().isInsideStructure(this.world, name, pos);
    }
}
