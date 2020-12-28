package net.gegy1000.terrarium.client;

import dev.gegy.gengen.api.GenericWorldType;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.message.TerrariumHandshakeMessage;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Terrarium.ID, value = Side.CLIENT)
public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final int STRUCTURES_BUTTON_ID = 4;

    private static boolean handshakeQueued;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (!handshakeQueued) return;

        if (MC.player != null && MC.player.ticksExisted > 1) {
            handshakeQueued = false;
            Terrarium.NETWORK.sendToServer(new TerrariumHandshakeMessage());
        }
    }

    @SubscribeEvent
    public static void onJoinWorld(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote && Terrarium.serverHasMod) {
            handshakeQueued = true;
        }
    }

    @SubscribeEvent
    public static void onGuiPostInit(GuiScreenEvent.InitGuiEvent.Post event) {
        GuiScreen currentScreen = event.getGui();
        if (currentScreen instanceof GuiCreateWorld) {
            GuiButton structuresButton = event.getButtonList().get(STRUCTURES_BUTTON_ID);
            int selectedWorldIndex = ClientProxy.getSelectedWorldType((GuiCreateWorld) currentScreen);
            GenericWorldType worldType = GenericWorldType.unwrap(WorldType.WORLD_TYPES[selectedWorldIndex]);
            if (worldType instanceof TerrariumWorldType) {
                structuresButton.visible = false;
            }
        }
    }

    @SubscribeEvent
    public static void onGuiButton(GuiScreenEvent.ActionPerformedEvent.Post event) {
        GuiScreen currentScreen = event.getGui();
        if (currentScreen instanceof GuiCreateWorld && event.getButton().id == 5) {
            GuiButton structuresButton = event.getButtonList().get(STRUCTURES_BUTTON_ID);
            int selectedWorldIndex = ClientProxy.getSelectedWorldType((GuiCreateWorld) currentScreen);
            TerrariumWorldType worldType = GenericWorldType.unwrapAs(WorldType.WORLD_TYPES[selectedWorldIndex], TerrariumWorldType.class);
            if (worldType != null) {
                if (worldType.isHidden() && !GuiScreen.isShiftKeyDown()) {
                    ClientProxy.actionPerformed(currentScreen, event.getButton());
                } else {
                    structuresButton.visible = false;
                }
            }
        }
    }
}
