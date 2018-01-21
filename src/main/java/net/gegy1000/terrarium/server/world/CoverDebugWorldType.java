package net.gegy1000.terrarium.server.world;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.generator.debug.CoverDebugBiomeProvider;
import net.gegy1000.terrarium.server.world.generator.debug.CoverDebugChunkGenerator;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;

import java.util.Random;

public class CoverDebugWorldType extends WorldType {
    public CoverDebugWorldType() {
        super(Terrarium.MODID + ".debug");
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String settings) {
        return new CoverDebugChunkGenerator(world, world.getSeed());
    }

    @Override
    public BiomeProvider getBiomeProvider(World world) {
        return new CoverDebugBiomeProvider(world);
    }

    @Override
    public boolean handleSlimeSpawnReduction(Random random, World world) {
        return true;
    }
}
