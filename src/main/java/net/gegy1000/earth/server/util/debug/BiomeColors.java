package net.gegy1000.earth.server.util.debug;

import net.minecraft.init.Biomes;
import net.minecraft.world.biome.Biome;

public final class BiomeColors {
    public static int get(Biome biome) {
        // Colors from: https://github.com/toolbox4minecraft/amidst
        if (biome == Biomes.OCEAN) return 0x000070;
        if (biome == Biomes.DEEP_OCEAN) return 0x000030;
        if (biome == Biomes.FROZEN_OCEAN) return 0x7070D6;
        if (biome == Biomes.RIVER) return 0x0000FF;
        if (biome == Biomes.FROZEN_RIVER) return 0xA0A0FF;

        if (biome == Biomes.PLAINS) return 0x8DB360;
        if (biome == Biomes.EXTREME_HILLS) return 0x606060;
        if (biome == Biomes.ICE_PLAINS) return 0xFFFFFF;
        if (biome == Biomes.ICE_MOUNTAINS) return 0xA0A0A0;
        if (biome == Biomes.DESERT) return 0xFA9418;
        if (biome == Biomes.DESERT_HILLS) return 0xD25F12;
        if (biome == Biomes.FOREST) return 0x056621;
        if (biome == Biomes.FOREST_HILLS) return 0x22551C;
        if (biome == Biomes.TAIGA) return 0x0B6659;
        if (biome == Biomes.TAIGA_HILLS) return 0x163933;
        if (biome == Biomes.COLD_TAIGA) return 0x31554A;
        if (biome == Biomes.COLD_TAIGA_HILLS) return 0x243F36;
        if (biome == Biomes.BIRCH_FOREST) return 0x307444;
        if (biome == Biomes.BIRCH_FOREST_HILLS) return 0x1F5F32;
        if (biome == Biomes.ROOFED_FOREST) return 0x40511A;
        if (biome == Biomes.JUNGLE) return 0x537B09;
        if (biome == Biomes.JUNGLE_HILLS) return 0x2C4205;
        if (biome == Biomes.JUNGLE_EDGE) return 0x628B17;
        if (biome == Biomes.SAVANNA) return 0xBDB25F;
        if (biome == Biomes.SAVANNA_PLATEAU) return 0xA79D64;
        if (biome == Biomes.SWAMPLAND) return 0x07F9B2;
        if (biome == Biomes.BEACH) return 0xFADE55;
        if (biome == Biomes.STONE_BEACH) return 0xA2A284;
        if (biome == Biomes.COLD_BEACH) return 0xFAF0C0;

        return 0xFF0000;
    }
}
