package net.gegy1000.earth.server.world.compatibility;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModGeneratorCompatibility {
    private static final Random RANDOM = new Random();

    public static void hookGenerators() {
        Set<IWorldGenerator> generatorsToHook = ModGenerators.getGenerators().stream()
                .filter(ModGeneratorCompatibility::shouldHook)
                .collect(Collectors.toSet());

        hookGenerators(generatorsToHook);
    }

    private static void hookGenerators(Collection<IWorldGenerator> generatorsToHook) {
        Set<IWorldGenerator> generators = ModGenerators.getGenerators();
        Map<IWorldGenerator, Integer> worldGeneratorIndex = ModGenerators.getGeneratorIndex();

        for (IWorldGenerator generator : generatorsToHook) {
            generators.remove(generator);
            Integer weight = worldGeneratorIndex.remove(generator);

            HookedGenerator hooked = new HookedGenerator(generator);
            generators.add(hooked);
            worldGeneratorIndex.put(hooked, weight);
        }

        ModGenerators.invalidateGenerators();
    }

    private static boolean shouldHook(IWorldGenerator generator) {
        String className = generator.getClass().getName();
        return className.startsWith("com.ferreusveritas.dynamictrees");
    }

    public static void runGeneratorsSafely(World world, ChunkPos columnPos, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        List<IWorldGenerator> generators = ModGenerators.getSortedGenerators();
        List<IWorldGenerator> erroredGenerators = null;

        long chunkSeed = getChunkSeed(world, columnPos);

        for (IWorldGenerator generator : generators) {
            RANDOM.setSeed(chunkSeed);
            try {
                generator.generate(RANDOM, columnPos.x, columnPos.z, world, chunkGenerator, chunkProvider);
            } catch (Exception e) {
                TerrariumEarth.LOGGER.error("Captured error from modded generator ({}) in safe mode: removing", generator, e);
                if (erroredGenerators == null) {
                    erroredGenerators = new ArrayList<>();
                }
                erroredGenerators.add(generator);
            }
        }

        if (erroredGenerators != null) {
            hookGenerators(erroredGenerators);
        }
    }

    private static long getChunkSeed(World world, ChunkPos columnPos) {
        long worldSeed = world.getSeed();
        RANDOM.setSeed(worldSeed);

        long xSeed = RANDOM.nextLong() >> 2 + 1L;
        long zSeed = RANDOM.nextLong() >> 2 + 1L;
        return (xSeed * columnPos.x + zSeed * columnPos.z) ^ worldSeed;
    }

    private static class HookedGenerator implements IWorldGenerator {
        private final IWorldGenerator inner;

        HookedGenerator(IWorldGenerator inner) {
            this.inner = inner;
        }

        @Override
        public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator generator, IChunkProvider provider) {
            if (!this.shouldExcludeFor(world)) {
                this.inner.generate(random, chunkX, chunkZ, world, generator, provider);
            }
        }

        private boolean shouldExcludeFor(World world) {
            return world.getWorldType() == TerrariumEarth.WORLD_TYPE && world.provider.getDimensionType() == DimensionType.OVERWORLD;
        }
    }
}
