package net.gegy1000.earth.server.world.composer.structure;

import net.gegy1000.earth.server.world.composer.structure.placement.MineshaftStructurePlacement;
import net.gegy1000.earth.server.world.composer.structure.placement.StructurePlacement;
import net.gegy1000.gengen.api.HeightFunction;
import net.gegy1000.terrarium.server.world.composer.structure.StructureComposer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.StructureMineshaftStart;
import net.minecraft.world.gen.structure.StructureStart;

import java.util.Random;

public final class MineshaftStructureComposer {
    public static StructureComposer create(World world, HeightFunction surfaceFunction) {
        StructurePlacement placement = new MineshaftStructurePlacement(0.004);

        return ColumnStructureComposer.builder()
                .setStructureName("Mineshaft")
                .setPlacement(placement)
                .setSurfaceFunction(surfaceFunction)
                .setStartConstructor(MineshaftStructureComposer::makeStart)
                .build(world);
    }

    private static StructureStart makeStart(World world, Random random, int chunkX, int chunkZ) {
        Biome biome = world.getBiome(new BlockPos((chunkX << 4) + 8, 64, (chunkZ << 4) + 8));
        MapGenMineshaft.Type type = biome instanceof BiomeMesa ? MapGenMineshaft.Type.MESA : MapGenMineshaft.Type.NORMAL;
        return new StructureMineshaftStart(world, random, chunkX, chunkZ, type);
    }
}
