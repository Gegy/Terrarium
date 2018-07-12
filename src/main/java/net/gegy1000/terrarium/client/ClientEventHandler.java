package net.gegy1000.terrarium.client;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.RemoteDataWarningGui;
import net.gegy1000.terrarium.server.config.TerrariumConfig;
import net.gegy1000.terrarium.server.message.TerrariumHandshakeMessage;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
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
    private static boolean handshakeQueued;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            gameTicks++;

            if (MC.player != null && MC.player.ticksExisted > 1) {
                if (awaitingLoad) {
                    awaitingLoad = false;
                    if (!TerrariumConfig.acceptedRemoteDataWarning) {
                        MC.displayGuiScreen(new RemoteDataWarningGui(MC.currentScreen));
                    }
                }
                if (handshakeQueued) {
                    handshakeQueued = false;
                    Terrarium.NETWORK.sendToServer(new TerrariumHandshakeMessage());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote) {
            if (world.getWorldType() instanceof TerrariumWorldType && MC.isIntegratedServerRunning()){
                awaitingLoad = true;
            }
            if (Terrarium.serverHasMod) {
                handshakeQueued = true;
            }
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

    @SubscribeEvent
    public static void onGuiButton(GuiScreenEvent.ActionPerformedEvent.Post event) {
        GuiScreen currentScreen = event.getGui();
        if (currentScreen instanceof GuiCreateWorld && event.getButton().id == 5) {
            GuiButton structuresButton = event.getButtonList().get(4);
            int selectedWorldIndex = ClientProxy.getSelectedWorldType((GuiCreateWorld) currentScreen);
            WorldType worldType = WorldType.WORLD_TYPES[selectedWorldIndex];
            if (worldType instanceof TerrariumWorldType) {
                TerrariumWorldType terrariumWorldType = (TerrariumWorldType) worldType;
                if (terrariumWorldType.isHidden() && !GuiScreen.isShiftKeyDown()) {
                    ClientProxy.actionPerformed(currentScreen, event.getButton());
                } else {
                    structuresButton.visible = false;
                }
            }
        }
    }

    public static int getGameTicks() {
        return ClientEventHandler.gameTicks;
    }
}
