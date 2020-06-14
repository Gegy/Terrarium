package net.gegy1000.earth.server.world.composer.structure;

import net.gegy1000.earth.server.world.composer.structure.placement.CellStructurePlacement;
import net.gegy1000.earth.server.world.composer.structure.placement.MineshaftStructurePlacement;
import net.gegy1000.earth.server.world.composer.structure.placement.StructurePlacement;
import net.gegy1000.gengen.api.HeightFunction;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.BiomeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class VanillaStructureComposers {
    private static final List<Biome> TEMPLE_BIOMES = Arrays.asList(
            Biomes.DESERT, Biomes.DESERT_HILLS,
            Biomes.JUNGLE, Biomes.JUNGLE_HILLS,
            Biomes.SWAMPLAND,
            Biomes.ICE_PLAINS,
            Biomes.COLD_TAIGA
    );

    private static final List<Biome> MONUMENT_BIOMES = Collections.singletonList(Biomes.DEEP_OCEAN);

    private static final List<Biome> STRONGHOLD_BIOMES = collectStrongholdBiomes();

    private static List<Biome> collectStrongholdBiomes() {
        List<Biome> biomes = new ArrayList<>();

        for (Biome biome : Biome.REGISTRY) {
            if (biome == null) continue;

            if (biome.getBaseHeight() > 0.0F && !BiomeManager.strongHoldBiomesBlackList.contains(biome)) {
                biomes.add(biome);
            }
        }

        for (Biome biome : BiomeManager.strongHoldBiomes) {
            if (!biomes.contains(biome)) {
                biomes.add(biome);
            }
        }

        biomes.remove(Biomes.VOID);

        return biomes;
    }

    public static StructureComposer village(World world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new CellStructurePlacement(32, 8, 10387312)
                .setPredicate(VanillaStructureComposers::canVillageSpawnAt);

        return ColumnStructureComposer.builder()
                .setStructureName("Village")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(VanillaStructureComposers::makeVillageStart)
                .build(world);
    }

    private static StructureStart makeVillageStart(World world, Random random, int chunkX, int chunkZ) {
        return new MapGenVillage.Start(world, random, chunkX, chunkZ, 0);
    }

    private static boolean canVillageSpawnAt(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        return world.getBiomeProvider().areBiomesViable(x, z, 0, MapGenVillage.VILLAGE_SPAWN_BIOMES);
    }

    public static StructureComposer temple(World world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new CellStructurePlacement(32, 8, 14357617)
                .setPredicate(VanillaStructureComposers::canTempleSpawnAt);

        return ColumnStructureComposer.builder()
                .setStructureName("Temple")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(VanillaStructureComposers::makeTempleStart)
                .build(world);
    }

    private static StructureStart makeTempleStart(World world, Random random, int chunkX, int chunkZ) {
        return new MapGenScatteredFeature.Start(world, random, chunkX, chunkZ);
    }

    private static boolean canTempleSpawnAt(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        return world.getBiomeProvider().areBiomesViable(x, z, 0, TEMPLE_BIOMES);
    }

    public static StructureComposer mineshaft(World world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new MineshaftStructurePlacement(0.004);

        return ColumnStructureComposer.builder()
                .setStructureName("Mineshaft")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(VanillaStructureComposers::makeMineshaftStart)
                .build(world);
    }

    private static StructureStart makeMineshaftStart(World world, Random random, int chunkX, int chunkZ) {
        Biome biome = world.getBiome(new BlockPos((chunkX << 4) + 8, 64, (chunkZ << 4) + 8));
        MapGenMineshaft.Type type = biome instanceof BiomeMesa ? MapGenMineshaft.Type.MESA : MapGenMineshaft.Type.NORMAL;
        return new StructureMineshaftStart(world, random, chunkX, chunkZ, type);
    }

    public static StructureComposer oceanMonument(World world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new CellStructurePlacement(32, 5, 10387313)
                .setPredicate(VanillaStructureComposers::canOceanMonumentSpawnAt);

        return ColumnStructureComposer.builder()
                .setStructureName("Monument")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(VanillaStructureComposers::makeOceanMonumentStart)
                .build(world);
    }

    private static StructureStart makeOceanMonumentStart(World world, Random random, int chunkX, int chunkZ) {
        return new StructureOceanMonument.StartMonument(world, random, chunkX, chunkZ);
    }

    private static boolean canOceanMonumentSpawnAt(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;

        return world.getBiomeProvider().areBiomesViable(x, z, 29, MONUMENT_BIOMES);
    }

    public static StructureComposer stronghold(World world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new CellStructurePlacement(128, 32, 1660845913)
                .setPredicate(VanillaStructureComposers::canStrongholdSpawnAt);

        return ColumnStructureComposer.builder()
                .setStructureName("Stronghold")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(VanillaStructureComposers::makeStrongholdStart)
                .build(world);
    }

    private static StructureStart makeStrongholdStart(World world, Random random, int chunkX, int chunkZ) {
        return new MapGenStronghold.Start(world, random, chunkX, chunkZ);
    }

    private static boolean canStrongholdSpawnAt(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        return world.getBiomeProvider().areBiomesViable(x, z, 16, STRONGHOLD_BIOMES);
    }
}
