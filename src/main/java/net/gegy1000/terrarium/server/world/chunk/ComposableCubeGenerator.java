package net.gegy1000.terrarium.server.world.chunk;

import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.CubePopulatorEvent;
import mcp.MethodsReturnNonnullByDefault;
import net.gegy1000.terrarium.server.world.chunk.populate.ColumnPopulateChunk;
import net.gegy1000.terrarium.server.world.chunk.prime.CubePrimeChunk;
import net.gegy1000.terrarium.server.world.generator.ChunkCompositionProcedure;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComposableCubeGenerator extends ComposableChunkGenerator implements ICubeGenerator {
    public ComposableCubeGenerator(World world) {
        super(world);
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        CubePrimer primer = new CubePrimer();

        RegionGenerationHandler regionHandler = this.regionHandler.get();
        regionHandler.prepareChunk(cubeX << 4, cubeZ << 4);

        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();

        CubePrimeChunk chunk = new CubePrimeChunk(new CubicPos(cubeX, cubeY, cubeZ), primer);
        compositionProcedure.composeSurface(regionHandler, chunk);

        // TODO
//        RegionGenerationHandler regionHandler = this.regionHandler.get();
//        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
//        compositionProcedure.composeStructures(this, primer, regionHandler, chunkX, chunkZ);

        return primer;
    }

    @Override
    public void generateColumn(Chunk column) {
        Biome[] biomeBuffer = this.provideBiomes(column.x, column.z);

        byte[] biomeArray = column.getBiomeArray();
        for (int i = 0; i < biomeBuffer.length; i++) {
            biomeArray[i] = (byte) Biome.getIdForBiome(biomeBuffer[i]);
        }
    }

    @Override
    public Biome[] provideBiomes(int chunkX, int chunkZ) {
        return this.world.getBiomeProvider().getBiomes(this.biomeBuffer, chunkX << 4, chunkZ << 4, 16, 16);
    }

    @Override
    public void populate(ICube cube) {
        if (!MinecraftForge.EVENT_BUS.post(new CubePopulatorEvent(this.world, cube))) {
            int globalX = cube.getX() << 4;
            int globalZ = cube.getZ() << 4;

            this.random.setSeed(cube.getX() * 341873128712L + cube.getZ() * 132897987541L);

            BlockFalling.fallInstantly = true;

            RegionGenerationHandler regionHandler = this.regionHandler.get();
            regionHandler.prepareChunk(globalX + 8, globalZ + 8);

            ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
            CubicPos pos = new CubicPos(cube.getX(), cube.getY(), cube.getZ());
            compositionProcedure.composeDecoration(this.world, regionHandler, new ColumnPopulateChunk(pos, this.world));

            // TODO
//            compositionProcedure.populateStructures(this.world, regionHandler, cube.getX(), cube.getZ());

            BlockFalling.fallInstantly = false;
        }
    }

    @Override
    public Box getFullPopulationRequirements(ICube cube) {
        return RECOMMENDED_FULL_POPULATOR_REQUIREMENT;
    }

    @Override
    public Box getPopulationPregenerationRequirements(ICube cube) {
        return RECOMMENDED_GENERATE_POPULATOR_REQUIREMENT;
    }

    @Override
    public void recreateStructures(ICube cube) {
        // TODO
    }

    @Override
    public void recreateStructures(Chunk column) {
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        return this.world.getBiome(pos).getSpawnableList(type);
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        ChunkCompositionProcedure compositionProcedure = this.compositionProcedure.get();
        return compositionProcedure.getNearestStructure(this.world, name, pos, findUnexplored);
    }
}
