package net.gegy1000.terrarium.server;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.getWorldType() instanceof TerrariumWorldType) {
            TerrariumWorldData worldData = ((TerrariumWorldType) world.getWorldType()).getWorldData(world);
            if (worldData != null) {
                world.setSpawnPoint(worldData.getSpawnpoint().toBlockPos());
            }
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (world.isRemote) {
            return;
        }

        if (world.getWorldType() instanceof TerrariumWorldType && world.provider.getDimensionType() == DimensionType.OVERWORLD) {
            event.addCapability(TerrariumCapabilities.WORLD_DATA_ID, new TerrariumWorldData.Implementation(world));
        }
    }
}
