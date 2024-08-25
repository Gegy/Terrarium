package dev.gegy.terrarium.world.generator.biome;

import com.mojang.serialization.MapCodec;
import dev.gegy.terrarium.Terrarium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;

public class TerrariumBiomeSources {
    public static final MapCodec<? extends BiomeSource> EARTH = Registry.register(BuiltInRegistries.BIOME_SOURCE, ResourceLocation.fromNamespaceAndPath(Terrarium.ID, "earth"), EarthBiomeSource.CODEC);

    public static void bootstrap() {
    }
}
