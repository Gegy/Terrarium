package net.gegy1000.earth.server;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class EarthDecorationEventHandler {
    public static boolean allowOreGeneration;

    private static Field sortedGeneratorListField;
    private static Method computeGeneratorsMethod;

    private static boolean generatorsHooked;
    public static List<IWorldGenerator> capturedGenerators;

    static {
        try {
            sortedGeneratorListField = ReflectionHelper.findField(GameRegistry.class, "sortedGeneratorList");
        } catch (ReflectionHelper.UnableToFindFieldException e) {
            TerrariumEarth.LOGGER.error("Failed to find sortedGeneratorList field", e);
        }
        try {
            computeGeneratorsMethod = ReflectionHelper.findMethod(GameRegistry.class, "computeSortedGeneratorList", null);
        } catch (ReflectionHelper.UnableToFindMethodException e) {
            TerrariumEarth.LOGGER.error("Failed to find computeSortedGeneratorList method", e);
        }
    }

    @SubscribeEvent
    public static void onChunkPopulatePre(PopulateChunkEvent.Pre event) {
        if (sortedGeneratorListField != null && computeGeneratorsMethod != null) {
            World world = event.getWorld();
            boolean shouldHook = world.getWorldType() == TerrariumEarth.EARTH_TYPE && world.provider.getDimensionType() == DimensionType.OVERWORLD;
            if (generatorsHooked != shouldHook) {
                try {
                    if (shouldHook) {
                        hookGenerators();
                    } else {
                        unhookGenerators();
                    }
                } catch (ReflectiveOperationException e) {
                    TerrariumEarth.LOGGER.error("Failed to hook generator list", e);
                }
            }
        }
    }

    private static void hookGenerators() throws ReflectiveOperationException {
        if (sortedGeneratorListField.get(null) == null) {
            computeGeneratorsMethod.invoke(null);
        }
        capturedGenerators = (List<IWorldGenerator>) sortedGeneratorListField.get(null);
        sortedGeneratorListField.set(null, Collections.emptyList());
        generatorsHooked = true;
    }

    private static void unhookGenerators() throws ReflectiveOperationException {
        sortedGeneratorListField.set(null, capturedGenerators);
        capturedGenerators = null;
        generatorsHooked = false;
    }

    @SubscribeEvent
    public static void onBiomeDecorate(DecorateBiomeEvent.Decorate event) {
        World world = event.getWorld();
        if (world.getWorldType() == TerrariumEarth.EARTH_TYPE && world.provider.getDimensionType() == DimensionType.OVERWORLD) {
            switch (event.getType()) {
                case GRASS:
                case TREE:
                    event.setResult(Event.Result.DENY);
                    break;
            }
        }
    }

    @SubscribeEvent
    public static void onGenerateOre(OreGenEvent.GenerateMinable event) {
        World world = event.getWorld();
        if (!allowOreGeneration && world.getWorldType() == TerrariumEarth.EARTH_TYPE && world.provider.getDimensionType() == DimensionType.OVERWORLD) {
            event.setResult(Event.Result.DENY);
        }
    }
}
