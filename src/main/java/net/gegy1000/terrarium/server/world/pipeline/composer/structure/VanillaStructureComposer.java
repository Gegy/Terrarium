package net.gegy1000.terrarium.server.world.pipeline.composer.structure;

import net.gegy1000.cubicglue.util.PseudoRandomMap;
import net.gegy1000.cubicglue.util.wrapper.OverworldGeneratorWrapper;
import net.gegy1000.earth.server.world.CubicGenerationFormat;
import net.gegy1000.earth.server.world.FeatureGenerationFormat;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.region.RegionGenerationHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
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

public class VanillaStructureComposer implements StructureComposer {
    private static final long STRUCTURE_SEED = 4826762579208967777L;

    private final World world;
    private final Random random = new Random(0);
    private final PseudoRandomMap randomMap;

    private final MapGenStructure strongholdGenerator;
    private final MapGenStructure villageGenerator;
    private final MapGenStructure mineshaftGenerator;
    private final MapGenStructure templeGenerator;
    private final MapGenStructure oceanMonumentGenerator;
    private final MapGenStructure woodlandMansionGenerator;

    public VanillaStructureComposer(World world, CubicGenerationFormat format) {
        this.world = world;
        this.randomMap = new PseudoRandomMap(world, STRUCTURE_SEED);

        ChunkGeneratorOverworld overworldWrapper = OverworldGeneratorWrapper.from(world);

        this.strongholdGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new MapGenStronghold(), InitMapGenEvent.EventType.STRONGHOLD);
        this.villageGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new MapGenVillage(), InitMapGenEvent.EventType.VILLAGE);
        this.mineshaftGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new MapGenMineshaft(), InitMapGenEvent.EventType.MINESHAFT);
        this.templeGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new MapGenScatteredFeature(), InitMapGenEvent.EventType.SCATTERED_FEATURE);
        this.oceanMonumentGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new StructureOceanMonument(), InitMapGenEvent.EventType.OCEAN_MONUMENT);
        this.woodlandMansionGenerator = (MapGenStructure) TerrainGen.getModdedMapGen(new WoodlandMansion(overworldWrapper), InitMapGenEvent.EventType.WOODLAND_MANSION);
    }

    @Override
    public void composeStructures(IChunkGenerator generator, ChunkPrimer primer, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        this.mineshaftGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.villageGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.strongholdGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.templeGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.oceanMonumentGenerator.generate(this.world, chunkX, chunkZ, primer);
        this.woodlandMansionGenerator.generate(this.world, chunkX, chunkZ, primer);
    }

    @Override
    public void populateStructures(World world, RegionGenerationHandler regionHandler, int chunkX, int chunkZ) {
        this.randomMap.initPosSeed(chunkX << 4, chunkZ << 4);
        this.random.setSeed(this.randomMap.next());

        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);

        this.mineshaftGenerator.generateStructure(world, this.random, chunkPos);
        this.villageGenerator.generateStructure(world, this.random, chunkPos);
        this.strongholdGenerator.generateStructure(world, this.random, chunkPos);
        this.templeGenerator.generateStructure(world, this.random, chunkPos);
        this.oceanMonumentGenerator.generateStructure(world, this.random, chunkPos);
        this.woodlandMansionGenerator.generateStructure(world, this.random, chunkPos);
    }

    @Override
    public boolean isInsideStructure(World world, String structureName, BlockPos pos) {
        MapGenStructure structure = this.getStructureGenerator(structureName);
        return structure != null && structure.isInsideStructure(pos);
    }

    @Nullable
    @Override
    public BlockPos getNearestStructure(World world, String structureName, BlockPos pos, boolean findUnexplored) {
        MapGenStructure structure = this.getStructureGenerator(structureName);
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

    @Override
    public RegionComponentType<?>[] getDependencies() {
        return new RegionComponentType[0];
    }
}
