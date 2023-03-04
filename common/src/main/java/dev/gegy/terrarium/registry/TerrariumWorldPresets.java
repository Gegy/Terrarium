package dev.gegy.terrarium.registry;

import dev.gegy.terrarium.Terrarium;
import dev.gegy.terrarium.world.generator.chunk.EarthChunkGenerator;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.util.Map;

public class TerrariumWorldPresets {
    public static final ResourceKey<WorldPreset> EARTH = createKey("earth");

    public static void bootstrap(final BootstapContext<WorldPreset> context) {
        final HolderGetter<DimensionType> dimensionType = context.lookup(Registries.DIMENSION_TYPE);
        final HolderGetter<NoiseGeneratorSettings> noiseSettings = context.lookup(Registries.NOISE_SETTINGS);

        context.register(EARTH, new WorldPreset(Map.of(
                LevelStem.OVERWORLD, new LevelStem(
                        dimensionType.getOrThrow(BuiltinDimensionTypes.OVERWORLD),
                        createDefaultEarthGenerator(context)
                ),
                LevelStem.NETHER, createNether(context, dimensionType, noiseSettings),
                LevelStem.END, createEnd(context, dimensionType, noiseSettings)
        )));
    }

    private static ChunkGenerator createDefaultEarthGenerator(final BootstapContext<WorldPreset> context) {
        return new EarthChunkGenerator(
                new FixedBiomeSource(context.lookup(Registries.BIOME).getOrThrow(Biomes.PLAINS))
        );
    }

    private static LevelStem createNether(final BootstapContext<WorldPreset> context, final HolderGetter<DimensionType> dimensionType, final HolderGetter<NoiseGeneratorSettings> noiseSettings) {
        return new LevelStem(
                dimensionType.getOrThrow(BuiltinDimensionTypes.NETHER),
                new NoiseBasedChunkGenerator(
                        MultiNoiseBiomeSource.createFromPreset(context.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getOrThrow(MultiNoiseBiomeSourceParameterLists.NETHER)),
                        noiseSettings.getOrThrow(NoiseGeneratorSettings.NETHER)
                )
        );
    }

    private static LevelStem createEnd(final BootstapContext<WorldPreset> context, final HolderGetter<DimensionType> dimensionType, final HolderGetter<NoiseGeneratorSettings> noiseSettings) {
        return new LevelStem(
                dimensionType.getOrThrow(BuiltinDimensionTypes.END),
                new NoiseBasedChunkGenerator(
                        TheEndBiomeSource.create(context.lookup(Registries.BIOME)),
                        noiseSettings.getOrThrow(NoiseGeneratorSettings.END)
                )
        );
    }

    private static ResourceKey<WorldPreset> createKey(final String name) {
        return ResourceKey.create(Registries.WORLD_PRESET, new ResourceLocation(Terrarium.ID, name));
    }
}
