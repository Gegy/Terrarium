package net.gegy1000.terrarium.server.world.chunk;

import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.util.Lazy;
import net.gegy1000.terrarium.server.world.chunk.populate.ColumnPopulateChunk;
import net.gegy1000.terrarium.server.world.chunk.prime.ColumnPrimeChunk;
import net.gegy1000.terrarium.server.world.chunk.prime.PrimeChunk;
import net.gegy1000.terrarium.server.world.generator.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComposableChunkGenerator implements IChunkGenerator, TerrariumChunkDelegate {
    protected final World world;
    protected final Random random;

    protected final Lazy<ChunkCompositionProcedure> compositionProcedure;

    protected final Lazy<RegionGenerationHandler> regionHandler;

    protected final Biome[] biomeBuffer = new Biome[16 * 16];

    public ComposableChunkGenerator(World world) {
        this.world = world;
        this.random = new Random(world.getWorldInfo().getSeed());

        this.compositionProcedure = new Lazy.WorldCap<>(world, TerrariumWorldData::getCompositionProcedure);

        this.regionHandler = new Lazy<>(() -> {
            TerrariumWorldData capability = this.world.getCapability(TerrariumCapabilities.worldDataCapability, null);
            if (capability != null) {
                return capability.getRegionHandler();
            }
            throw new IllegalStateException("Tried to load RegionGenerationHandler before it was present");
        });
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        ChunkPrimer primer = this.generatePrimer(chunkX, chunkZ);

        Chunk chunk = new Chunk(this.world, primer, chunkX, chunkZ);

        Biome[] biomeBuffer = this.provideBiomes(chunkX, chunkZ);

        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeBuffer.length; i++) {
            biomeArray[i] = (byte) Biome.getIdForBiome(biomeBuffer[i]);
        }

        chunk.generateSkylightMap();

        return chunk;
    }

    public ChunkPrimer generatePrimer(int chunkX, int chunkZ) {
        ChunkPrimer primer = new ChunkPrimer();
        this.populateTerrain(chunkX, chunkZ, primer);

        RegionGenerationHandler regionHandler = this.regionHandler.get();
        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        compositionProcedure.composeStructures(this, primer, regionHandler, chunkX, chunkZ);

        return primer;
    }

    @Override
    public void populateTerrain(int chunkX, int chunkZ, ChunkPrimer primer) {
        RegionGenerationHandler regionHandler = this.regionHandler.get();
        regionHandler.prepareChunk(chunkX << 4, chunkZ << 4);

        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        for (int chunkY = 0; chunkY < 16; chunkY++) {
            PrimeChunk chunk = new ColumnPrimeChunk(new CubicPos(chunkX, chunkY, chunkZ), primer);
            compositionProcedure.composeSurface(regionHandler, chunk);
        }
    }

    public Biome[] provideBiomes(int chunkX, int chunkZ) {
        return this.world.getBiomeProvider().getBiomes(this.biomeBuffer, chunkX << 4, chunkZ << 4, 16, 16);
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        int globalX = chunkX << 4;
        int globalZ = chunkZ << 4;

        this.random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);

        BlockFalling.fallInstantly = true;

        RegionGenerationHandler regionHandler = this.regionHandler.get();
        regionHandler.prepareChunk(globalX + 8, globalZ + 8);

        ForgeEventFactory.onChunkPopulate(true, this, this.world, this.random, chunkX, chunkZ, false);

        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        for (int chunkY = 0; chunkY < 16; chunkY++) {
            ColumnPopulateChunk chunk = new ColumnPopulateChunk(new CubicPos(chunkX, chunkY, chunkZ), this.world);
            compositionProcedure.composeDecoration(this.world, regionHandler, chunk);
        }

        compositionProcedure.populateStructures(this.world, regionHandler, chunkX, chunkZ);

        ForgeEventFactory.onChunkPopulate(false, this, this.world, this.random, chunkX, chunkZ, false);

        BlockFalling.fallInstantly = false;
    }

    @Override
    public boolean generateStructures(Chunk chunk, int chunkX, int chunkZ) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return this.world.getBiome(pos).getSpawnableList(creatureType);
    }

    @Override
    public BlockPos getNearestStructurePos(World world, String structureName, BlockPos pos, boolean findUnexplored) {
        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        return compositionProcedure.getNearestStructure(world, structureName, pos, findUnexplored);
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        return compositionProcedure.isInsideStructure(world, structureName, pos);
    }

    @Override
    public void recreateStructures(Chunk chunk, int chunkX, int chunkZ) {
        RegionGenerationHandler regionHandler = this.regionHandler.get();
        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        compositionProcedure.composeStructures(this, null, regionHandler, chunkX, chunkZ);
    }
}
