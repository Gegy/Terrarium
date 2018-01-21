package net.gegy1000.terrarium.server.world.generator;

import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.map.LatitudinalZone;
import net.gegy1000.terrarium.server.map.cover.CoverType;
import net.gegy1000.terrarium.server.util.Coordinate;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import java.util.List;

public class EarthChunkGenerator extends TerrariumChunkGenerator {
    private final EarthGenerationSettings settings;

    private final boolean fastGenerate;

    private final Lazy<EarthGenerationHandler> generationHandler = new Lazy<>(() -> {
        TerrariumWorldData capability = this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
        if (capability != null) {
            return capability.getGenerationHandler();
        }
        throw new RuntimeException("Tried to load EarthGenerationHandler before it was present");
    });

    public EarthChunkGenerator(World world, long seed, String settingsString, boolean fastGenerate) {
        super(world, seed, false);

        this.settings = EarthGenerationSettings.deserialize(settingsString);
        this.fastGenerate = fastGenerate;
    }

    @Override
    public boolean generateStructures(Chunk chunk, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getNearestStructurePos(World world, String structureName, BlockPos pos, boolean findUnexplored) {
        return pos;
    }

    @Override
    public void recreateStructures(Chunk chunk, int x, int z) {
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        return false;
    }

    @Override
    protected void populateHeightRegion(int[] heightBuffer, int chunkX, int chunkZ) {
        this.generationHandler.get().populateHeightRegion(heightBuffer, chunkX, chunkZ);
    }

    @Override
    protected void populateCoverRegion(CoverType[] coverBuffer, int chunkX, int chunkZ) {
        this.generationHandler.get().populateCoverRegion(coverBuffer, chunkX, chunkZ);
    }

    @Override
    protected LatitudinalZone getLatitudinalZone(int x, int z) {
        int offset = this.random.nextInt(128) - this.random.nextInt(128);
        Coordinate chunkCoordinate = Coordinate.fromBlock(this.settings, x + 8, z + 8 + offset);
        return LatitudinalZone.get(chunkCoordinate);
    }

    @Override
    protected int getOceanHeight() {
        return this.generationHandler.get().getOceanHeight();
    }

    @Override
    protected boolean shouldDecorate() {
        return this.settings.decorate;
    }

    @Override
    protected boolean shouldFastGenerate() {
        return this.fastGenerate;
    }
}
