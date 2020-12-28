package net.gegy1000.earth.server.world.composer.structure;

import dev.gegy.gengen.api.HeightFunction;
import net.gegy1000.earth.server.world.composer.structure.placement.CellStructurePlacement;
import net.gegy1000.earth.server.world.composer.structure.placement.StructurePlacement;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class OceanMonumentStructureComposer {
    private static final List<Biome> MONUMENT_BIOMES = Collections.singletonList(Biomes.DEEP_OCEAN);

    private static final Biome.SpawnListEntry GUARDIAN = new Biome.SpawnListEntry(EntityGuardian.class, 1, 2, 4);

    public static StructureComposer create(WorldServer world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new CellStructurePlacement(32, 5, 10387313)
                .setPredicate(OceanMonumentStructureComposer::canSpawnAt);

        return ColumnStructureComposer.builder()
                .setStructureName("Monument")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(OceanMonumentStructureComposer::makeStart)
                .addCreatures(EnumCreatureType.MONSTER, GUARDIAN)
                .build(world);
    }

    private static StructureStart makeStart(World world, Random random, int chunkX, int chunkZ) {
        return new StructureOceanMonument.StartMonument(world, random, chunkX, chunkZ);
    }

    private static boolean canSpawnAt(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;

        return world.getBiomeProvider().areBiomesViable(x, z, 16, MONUMENT_BIOMES);
    }
}
