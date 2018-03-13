package net.gegy1000.earth.server.world;

import net.gegy1000.terrarium.Terrarium;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.Random;

// TODO: Implement generators for the debug world type
public class CoverDebugWorldType extends WorldType {
    public CoverDebugWorldType() {
        super(Terrarium.MODID + ".debug");
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String settings) {
//        return new ComposableChunkGenerator(world);
        return null;
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
//        return new ComposableBiomeProvider(world);
        return null;
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
        return true;
    }
}
