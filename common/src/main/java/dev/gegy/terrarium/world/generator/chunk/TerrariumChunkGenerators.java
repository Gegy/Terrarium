package dev.gegy.terrarium.world.generator.chunk;

import com.mojang.serialization.MapCodec;
import dev.gegy.terrarium.Terrarium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class TerrariumChunkGenerators {
    public static final MapCodec<? extends ChunkGenerator> EARTH = Registry.register(BuiltInRegistries.CHUNK_GENERATOR, ResourceLocation.fromNamespaceAndPath(Terrarium.ID, "earth"), EarthChunkGenerator.CODEC);

    public static void bootstrap() {
    }
}
