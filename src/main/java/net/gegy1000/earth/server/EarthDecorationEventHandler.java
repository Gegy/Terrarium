package net.gegy1000.earth.server;

import net.gegy1000.earth.TerrariumEarth;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EarthDecorationEventHandler {
    public static boolean allowOreGeneration;

    @SubscribeEvent
    public static void onBiomeDecorate(DecorateBiomeEvent.Decorate event) {
        World world = event.getWorld();
        if (world.provider.getDimensionType() == DimensionType.OVERWORLD && world.getWorldType() == TerrariumEarth.EARTH_TYPE) {
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
        if (allowOreGeneration && world.provider.getDimensionType() == DimensionType.OVERWORLD && world.getWorldType() == TerrariumEarth.EARTH_TYPE) {
            event.setResult(Event.Result.DENY);
        }
    }
}
