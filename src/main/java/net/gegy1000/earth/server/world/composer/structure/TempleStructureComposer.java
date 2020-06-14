package net.gegy1000.earth.server.world.composer.structure;

import net.gegy1000.earth.server.world.composer.structure.placement.CellStructurePlacement;
import net.gegy1000.earth.server.world.composer.structure.placement.StructurePlacement;
import net.gegy1000.gengen.api.HeightFunction;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class TempleStructureComposer {
    private static final List<Biome> TEMPLE_BIOMES = Arrays.asList(
            Biomes.DESERT, Biomes.DESERT_HILLS,
            Biomes.JUNGLE, Biomes.JUNGLE_HILLS,
            Biomes.SWAMPLAND,
            Biomes.ICE_PLAINS,
            Biomes.COLD_TAIGA
    );

    public static StructureComposer create(World world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new CellStructurePlacement(32, 8, 14357617)
                .setPredicate(TempleStructureComposer::canSpawnAt);

        return ColumnStructureComposer.builder()
                .setStructureName("Temple")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(TempleStructureComposer::makeStart)
                .build(world);
    }

    private static StructureStart makeStart(World world, Random random, int chunkX, int chunkZ) {
        return new MapGenScatteredFeature.Start(world, random, chunkX, chunkZ);
    }

    private static boolean canSpawnAt(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        return world.getBiomeProvider().areBiomesViable(x, z, 0, TEMPLE_BIOMES);
    }
}
