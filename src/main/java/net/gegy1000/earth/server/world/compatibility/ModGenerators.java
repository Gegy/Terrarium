package net.gegy1000.earth.server.world.compatibility;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ModGenerators {
    private static Set<IWorldGenerator> worldGenerators = new HashSet<>();
    private static Map<IWorldGenerator, Integer> worldGeneratorIndex = new HashMap<>();

    private static Field sortedGeneratorListField;
    private static Method computeSortedGeneratorListMethod;

    static {
        try {
            Field field = GameRegistry.class.getDeclaredField("worldGenerators");
            field.setAccessible(true);
            worldGenerators = (Set<IWorldGenerator>) field.get(null);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to get worldGenerators field", e);
        }

        try {
            Field field = GameRegistry.class.getDeclaredField("worldGeneratorIndex");
            field.setAccessible(true);
            worldGeneratorIndex = (Map<IWorldGenerator, Integer>) field.get(null);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to get worldGeneratorIndex field", e);
        }

        try {
            sortedGeneratorListField = GameRegistry.class.getDeclaredField("sortedGeneratorList");
            sortedGeneratorListField.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to find sortedGeneratorList field", e);
        }

        try {
            computeSortedGeneratorListMethod = GameRegistry.class.getDeclaredMethod("computeSortedGeneratorList");
            computeSortedGeneratorListMethod.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to find computeSortedGeneratorList method", e);
        }
    }

    public static Set<IWorldGenerator> getGenerators() {
        return worldGenerators;
    }

    public static Map<IWorldGenerator, Integer> getGeneratorIndex() {
        return worldGeneratorIndex;
    }

    @SuppressWarnings("unchecked")
    public static List<IWorldGenerator> getSortedGenerators() {
        if (sortedGeneratorListField == null || computeSortedGeneratorListMethod == null) {
            return Collections.emptyList();
        }

        try {
            if (sortedGeneratorListField.get(null) == null) {
                computeSortedGeneratorListMethod.invoke(null);
            }

            return (List<IWorldGenerator>) sortedGeneratorListField.get(null);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.warn("Failed to get sorted modded world generators", e);
        }

        return Collections.emptyList();
    }

    public static void invalidateGenerators() {
        if (sortedGeneratorListField != null) {
            try {
                sortedGeneratorListField.set(null, null);
            } catch (ReflectiveOperationException e) {
                TerrariumEarth.LOGGER.warn("Failed to invalidate modded generator index", e);
            }
        }
    }
}
