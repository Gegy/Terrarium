package net.gegy1000.terrarium.client;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.RemoteDataWarningGui;
import net.gegy1000.terrarium.server.config.TerrariumConfig;
import net.gegy1000.terrarium.server.world.EarthWorldType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Terrarium.MODID, value = Side.CLIENT)
public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static int gameTicks = 0;

    private static boolean awaitingLoad;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            gameTicks++;

            if (awaitingLoad && MC.player != null && MC.player.ticksExisted > 10) {
                awaitingLoad = false;
                if (!TerrariumConfig.acceptedRemoteDataWarning) {
                    MC.displayGuiScreen(new RemoteDataWarningGui(MC.currentScreen));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote && world.getWorldType() instanceof EarthWorldType && MC.isIntegratedServerRunning()) {
            awaitingLoad = true;
        }
    }

    @SubscribeEvent
    public static void onGuiChange(GuiOpenEvent event) {
        GuiScreen currentScreen = MC.currentScreen;
        if (currentScreen instanceof RemoteDataWarningGui && !((RemoteDataWarningGui) currentScreen).isComplete()) {
            event.setCanceled(true);
            ((RemoteDataWarningGui) currentScreen).setParent(event.getGui());
        }
    }

    public static int getGameTicks() {
        return ClientEventHandler.gameTicks;
    }
}
