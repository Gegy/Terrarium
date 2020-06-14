package net.gegy1000.earth.server.world.composer.structure;

import net.gegy1000.gengen.api.HeightFunction;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Random;

public final class VanillaStructureComposers {
    public static StructureComposer village(World world, HeightFunction surfaceFunction) {
        CellStructurePlacement placement = new CellStructurePlacement(32, 8, 10387312)
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
}
