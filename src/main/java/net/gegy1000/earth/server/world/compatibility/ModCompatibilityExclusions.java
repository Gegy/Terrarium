package net.gegy1000.earth.server.world.compatibility;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public final class ModCompatibilityExclusions {
    private static Field worldGeneratorsField;
    private static Field worldGeneratorIndexField;
    private static Field sortedGeneratorListField;

    static {
        try {
            worldGeneratorsField = GameRegistry.class.getDeclaredField("worldGenerators");
            worldGeneratorsField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to find worldGenerators field", e);
        }
        try {
            worldGeneratorIndexField = GameRegistry.class.getDeclaredField("worldGeneratorIndex");
            worldGeneratorIndexField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to find worldGeneratorIndex field", e);
        }
        try {
            sortedGeneratorListField = GameRegistry.class.getDeclaredField("sortedGeneratorList");
            sortedGeneratorListField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to find sortedGeneratorList field", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void hookGenerators() {
        try {
            Set<IWorldGenerator> worldGenerators = (Set<IWorldGenerator>) worldGeneratorsField.get(null);
            Map<IWorldGenerator, Integer> worldGeneratorIndex = (Map<IWorldGenerator, Integer>) worldGeneratorIndexField.get(null);

            Set<IWorldGenerator> generatorsToHook = worldGenerators.stream()
                    .filter(ModCompatibilityExclusions::shouldHook)
                    .collect(Collectors.toSet());

            for (IWorldGenerator generator : generatorsToHook) {
                worldGenerators.remove(generator);
                Integer weight = worldGeneratorIndex.remove(generator);

                HookedGenerator hooked = new HookedGenerator(generator);
                worldGenerators.add(hooked);
                worldGeneratorIndex.put(hooked, weight);
            }

            // invalidate cache
            sortedGeneratorListField.set(null, null);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.warn("Failed to hook modded world generators", e);
        }
    }

    private static boolean shouldHook(IWorldGenerator generator) {
        String className = generator.getClass().getName();
        return className.startsWith("com.ferreusveritas.dynamictrees");
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
