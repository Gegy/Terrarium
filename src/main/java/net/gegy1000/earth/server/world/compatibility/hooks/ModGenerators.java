package net.gegy1000.earth.server.world.compatibility.hooks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.gegy.gengen.api.GenericWorldType;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.world.EarthWorldType;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/*
    This is the site of terrible, terrible hacks. Do not look too closely.
 */
@Mod.EventBusSubscriber(modid = TerrariumEarth.ID)
public final class ModGenerators {
    private static Set<IWorldGenerator> worldGenerators = ImmutableSet.of();

    private static final MethodHandle GET_SORTED_GENERATOR_LIST;
    private static final MethodHandle SET_SORTED_GENERATOR_LIST;
    private static final MethodHandle COMPUTE_SORTED_GENERATORS;

    private static final List<IWorldGenerator> HIDDEN_GENERATORS = ImmutableList.of();
    private static List<IWorldGenerator> sortedGenerators;

    static {
        try {
            Field field = GameRegistry.class.getDeclaredField("worldGenerators");
            field.setAccessible(true);
            worldGenerators = (Set<IWorldGenerator>) field.get(null);
        } catch (ReflectiveOperationException e) {
            TerrariumEarth.LOGGER.error("Failed to get worldGenerators field", e);
        }

        try {
            Field sortedGeneratorListField = GameRegistry.class.getDeclaredField("sortedGeneratorList");
            sortedGeneratorListField.setAccessible(true);

            GET_SORTED_GENERATOR_LIST = MethodHandles.lookup().unreflectGetter(sortedGeneratorListField);
            SET_SORTED_GENERATOR_LIST = MethodHandles.lookup().unreflectSetter(sortedGeneratorListField);

            Method computeSortedGeneratorsMethod = GameRegistry.class.getDeclaredMethod("computeSortedGeneratorList");
            computeSortedGeneratorsMethod.setAccessible(true);

            COMPUTE_SORTED_GENERATORS = MethodHandles.lookup().unreflect(computeSortedGeneratorsMethod);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to reflect GameRegistry generator lists", e);
        }
    }

    public static Set<IWorldGenerator> getGenerators() {
        return worldGenerators;
    }

    public static List<IWorldGenerator> getAndHookSortedGenerators() {
        try {
            List<IWorldGenerator> forgeGenerators = getOrComputeSortedGenerators();
            if (!areGeneratorsHooked(forgeGenerators)) {
                SET_SORTED_GENERATOR_LIST.invokeExact(HIDDEN_GENERATORS);
                sortedGenerators = forgeGenerators;
            }
            return sortedGenerators;
        } catch (Throwable t) {
            TerrariumEarth.LOGGER.warn("Failed to hook sorted modded world generators", t);
            return Collections.emptyList();
        }
    }

    public static void restoreSortedGenerators() {
        try {
            SET_SORTED_GENERATOR_LIST.invokeExact((List<IWorldGenerator>) null);
        } catch (Throwable t) {
            TerrariumEarth.LOGGER.warn("Failed to restore sorted modded world generators", t);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<IWorldGenerator> getOrComputeSortedGenerators() throws Throwable {
        List<IWorldGenerator> generators = (List<IWorldGenerator>) GET_SORTED_GENERATOR_LIST.invokeExact();
        if (generators == null) {
            COMPUTE_SORTED_GENERATORS.invokeExact();
            generators = (List<IWorldGenerator>) GET_SORTED_GENERATOR_LIST.invokeExact();
        }
        return generators;
    }

    private static boolean areGeneratorsHooked(List<IWorldGenerator> forgeGenerators) {
        return forgeGenerators == HIDDEN_GENERATORS;
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event) {
        EarthWorldType earth = GenericWorldType.unwrapAs(event.getWorld().getWorldType(), EarthWorldType.class);
        if (earth != null) {
            ModGenerators.restoreSortedGenerators();
        }
    }
}
