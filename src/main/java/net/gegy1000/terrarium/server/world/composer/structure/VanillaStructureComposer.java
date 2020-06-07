package net.gegy1000.terrarium.server.world.composer.structure;

import net.gegy1000.gengen.api.CubicPos;
import net.gegy1000.gengen.api.writer.ChunkPopulationWriter;
import net.gegy1000.gengen.api.writer.ChunkPrimeWriter;
import net.gegy1000.gengen.util.SpatialRandom;
import net.gegy1000.gengen.util.wrapper.OverworldGeneratorWrapper;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.WoodlandMansion;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import javax.annotation.Nullable;
import java.util.Random;

// TODO: implement
public class VanillaStructureComposer implements StructureComposer {
    private static final long STRUCTURE_SEED = 4826762579208967777L;

    private final World world;
    private final Random random = new Random(0);
    private final SpatialRandom spatialRandom;

    private final MapGenStructure strongholdGenerator;
    private final MapGenStructure villageGenerator;
    private final MapGenStructure mineshaftGenerator;
    private final MapGenStructure templeGenerator;
    private final MapGenStructure oceanMonumentGenerator;
    private final MapGenStructure woodlandMansionGenerator;

    public VanillaStructureComposer(World world) {
        this.world = world;
        this.spatialRandom = new SpatialRandom(world, STRUCTURE_SEED);

        ChunkGeneratorOverworld overworldWrapper = OverworldGeneratorWrapper.from(world);
        if (overworldWrapper == null) {
            Terrarium.LOGGER.warn("Failed to unwrap Terrarium chunk generator! This is very likely to be caused by a mod compatibility issue");
            Terrarium.LOGGER.warn("Attempting to continue on... Some vanilla generation integration may not work!");

            IChunkProvider chunkProvider = world.getChunkProvider();
            if (chunkProvider instanceof ChunkProviderServer) {
                IChunkGenerator chunkGenerator = ((ChunkProviderServer) chunkProvider).chunkGenerator;
                Terrarium.LOGGER.warn("Responsible chunk generator: {} ({})", chunkGenerator, chunkGenerator.getClass());
            } else {
                Terrarium.LOGGER.warn("Failed to access chunk generator for diagnostics. Chunk provider was: {} ({})", chunkProvider, chunkProvider.getClass());
            }
        }

        this.strongholdGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new MapGenStronghold(), InitMapGenEvent.EventType.STRONGHOLD);
        this.villageGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new MapGenVillage(), InitMapGenEvent.EventType.VILLAGE);
        this.mineshaftGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new MapGenMineshaft(), InitMapGenEvent.EventType.MINESHAFT);
        this.templeGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new MapGenScatteredFeature(), InitMapGenEvent.EventType.SCATTERED_FEATURE);
        this.oceanMonumentGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new StructureOceanMonument(), InitMapGenEvent.EventType.OCEAN_MONUMENT);

        if (overworldWrapper != null) {
            this.woodlandMansionGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new WoodlandMansion(overworldWrapper), InitMapGenEvent.EventType.WOODLAND_MANSION);
        } else {
            this.woodlandMansionGenerator = null;
        }
    }

    @Override
    public void prepareStructures(TerrariumWorld terrarium, CubicPos pos) {
        /*int chunkX = pos.getX();
        int chunkZ = pos.getZ();

        this.mineshaftGenerator.generate(this.world, chunkX, chunkZ, null);
        this.villageGenerator.generate(this.world, chunkX, chunkZ, null);
        this.strongholdGenerator.generate(this.world, chunkX, chunkZ, null);
        this.templeGenerator.generate(this.world, chunkX, chunkZ, null);
        this.oceanMonumentGenerator.generate(this.world, chunkX, chunkZ, null);
        this.woodlandMansionGenerator.generate(this.world, chunkX, chunkZ, null);*/
    }

    @Override
    public void primeStructures(TerrariumWorld terrarium, CubicPos pos, ChunkPrimeWriter writer) {
        /*int chunkX = pos.getX();
        int chunkZ = pos.getZ();

        this.mineshaftGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.villageGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.strongholdGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.templeGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.oceanMonumentGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.woodlandMansionGenerator.generate(this.world, chunkX, chunkZ, primer);*/
    }

    @Override
    public void populateStructures(TerrariumWorld terrarium, CubicPos pos, ChunkPopulationWriter writer) {
        /*this.randomMap.initPosSeed(pos.getMinX(), pos.getMinZ());
        this.random.setSeed(this.randomMap.next());

        ChunkPos chunkPos = new ChunkPos(pos.getX(), pos.getZ());

        this.mineshaftGenerator.generateStructure(this.world, this.random, chunkPos);
        this.villageGenerator.generateStructure(this.world, this.random, chunkPos);
        this.strongholdGenerator.generateStructure(this.world, this.random, chunkPos);
        this.templeGenerator.generateStructure(this.world, this.random, chunkPos);
        this.oceanMonumentGenerator.generateStructure(this.world, this.random, chunkPos);
        this.woodlandMansionGenerator.generateStructure(this.world, this.random, chunkPos);*/
    }

    @Override
    public boolean isInsideStructure(TerrariumWorld terrarium, World world, String name, BlockPos pos) {
        MapGenStructure structure = this.getStructureGenerator(name);
        return structure != null && structure.isInsideStructure(pos);
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(TerrariumWorld terrarium, World world, String name, BlockPos pos, boolean findUnexplored) {
        MapGenStructure structure = this.getStructureGenerator(name);
        return structure != null ? structure.getNearestStructurePos(world, pos, findUnexplored) : null;
    }

    private MapGenStructure getStructureGenerator(String structureName) {
        switch (structureName) {
            case "Stronghold":
                return this.strongholdGenerator;
            case "Mansion":
                return this.woodlandMansionGenerator;
            case "Monument":
                return this.oceanMonumentGenerator;
            case "Village":
                return this.villageGenerator;
            case "Mineshaft":
                return this.mineshaftGenerator;
            case "Temple":
                return this.templeGenerator;
        }
        return null;
    }
}
