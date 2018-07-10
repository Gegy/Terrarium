package net.gegy1000.terrarium.server;

import com.google.gson.JsonSyntaxException;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.capability.TerrariumWorldData;
import net.gegy1000.terrarium.server.message.TerrariumLoadingStateMessage;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingState;
import net.gegy1000.terrarium.server.world.pipeline.source.LoadingStateHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Terrarium.MODID)
public class ServerEventHandler {
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        World world = event.getWorld();
        if (ServerEventHandler.shouldHandle(world)) {
            TerrariumWorldData worldData = ((TerrariumWorldType) world.getWorldType()).getWorldData(world);
            if (worldData != null) {
                Coordinate spawnPosition = worldData.getSpawnPosition();
                if (spawnPosition != null) {
                    world.setSpawnPoint(spawnPosition.toBlockPos());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        if (ServerEventHandler.shouldHandle(world)) {
            try {
                TerrariumWorldType worldType = (TerrariumWorldType) world.getWorldType();
                event.addCapability(TerrariumCapabilities.WORLD_DATA_ID, new TerrariumWorldData.Implementation(world, worldType));
            } catch (JsonSyntaxException e) {
                Terrarium.LOGGER.error("Failed to construct generator", e);
            }
        }
    }
    
    private static LoadingState lastState = null;
    @SubscribeEvent
    public static void onWorldTick(TickEvent event) {
    	if (event.side == Side.SERVER) {
    		LoadingState state = LoadingStateHandler.checkState();
    		if (state != lastState) {
	    		TerrariumLoadingStateMessage message = new TerrariumLoadingStateMessage(state);
	    		for (EntityPlayer player : TerrariumHandshakeTracker.getFriends()) {
	    			Terrarium.network.sendTo(message, (EntityPlayerMP) player);
	    		}
	    		lastState = state;
    		}
    	}
    }

    private static boolean shouldHandle(World world) {
        return !world.isRemote && world.provider.getDimensionType() == DimensionType.OVERWORLD && world.getWorldType() instanceof TerrariumWorldType;
    }
}
