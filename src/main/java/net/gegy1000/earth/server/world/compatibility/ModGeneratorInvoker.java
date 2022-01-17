package net.gegy1000.earth.server.world.compatibility;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.compatibility.hooks.ModGenerators;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.List;
import java.util.Random;
import java.util.Set;

public final class ModGeneratorInvoker {
    private static final Random RANDOM = new Random();

    private static final Set<IWorldGenerator> EXCLUDED_GENERATORS = new ReferenceOpenHashSet<>();

    public static void collectGeneratorExclusions() {
        ModGenerators.getGenerators().stream()
                .filter(ModGeneratorInvoker::shouldExclude)
                .forEach(EXCLUDED_GENERATORS::add);
    }

    private static boolean shouldExclude(IWorldGenerator generator) {
        String className = generator.getClass().getName();
        return className.startsWith("com.ferreusveritas.dynamictrees");
    }

    public static void runGenerators(World world, ChunkPos columnPos, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        List<IWorldGenerator> generators = ModGenerators.getAndHookSortedGenerators();
        long chunkSeed = getChunkSeed(world, columnPos);

        for (IWorldGenerator generator : generators) {
            if (EXCLUDED_GENERATORS.contains(generator)) {
                continue;
            }

            RANDOM.setSeed(chunkSeed);

            try {
                generator.generate(RANDOM, columnPos.x, columnPos.z, world, chunkGenerator, chunkProvider);
            } catch (Exception e) {
                TerrariumEarth.LOGGER.error("Captured error from modded generator ({}) in compatibility mode: excluding from future generation", generator, e);
                EXCLUDED_GENERATORS.add(generator);
            }
        }
    }

    private static long getChunkSeed(World world, ChunkPos columnPos) {
        long worldSeed = world.getSeed();
        RANDOM.setSeed(worldSeed);

        long xSeed = RANDOM.nextLong() >> 2 + 1L;
        long zSeed = RANDOM.nextLong() >> 2 + 1L;
        return (xSeed * columnPos.x + zSeed * columnPos.z) ^ worldSeed;
    }
}
