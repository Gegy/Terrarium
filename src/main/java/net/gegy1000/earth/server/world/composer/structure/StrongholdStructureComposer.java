package net.gegy1000.earth.server.world.composer.structure;

import dev.gegy.gengen.api.HeightFunction;
import net.gegy1000.earth.server.world.composer.structure.placement.CellStructurePlacement;
import net.gegy1000.earth.server.world.composer.structure.placement.StructurePlacement;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.StructureStart;
import net.minecraftforge.common.BiomeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class StrongholdStructureComposer {
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

    public static StructureComposer create(WorldServer world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new CellStructurePlacement(128, 32, 1660845913)
                .setPredicate(StrongholdStructureComposer::canSpawnAt);

        return ColumnStructureComposer.builder()
                .setStructureName("Stronghold")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(StrongholdStructureComposer::makeStart)
                .build(world);
    }

    private static StructureStart makeStart(World world, Random random, int chunkX, int chunkZ) {
        return new MapGenStronghold.Start(world, random, chunkX, chunkZ);
    }

    private static boolean canSpawnAt(World world, int chunkX, int chunkZ) {
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        return world.getBiomeProvider().areBiomesViable(x, z, 16, STRONGHOLD_BIOMES);
    }
}
