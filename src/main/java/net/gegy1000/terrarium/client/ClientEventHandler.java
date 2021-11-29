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

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Terrarium.ID, value = Side.CLIENT)
public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();
    private static final int WORLD_TYPE_BUTTON_ID = 5;

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
            GuiCreateWorld createWorld = (GuiCreateWorld) currentScreen;
            GenericWorldType worldType = getSelectedWorldType(createWorld);
            if (worldType != null) {
                hideStructuresButton(createWorld);
            }
        }
    }

    @SubscribeEvent
    public static void onGuiButton(GuiScreenEvent.ActionPerformedEvent.Post event) {
        GuiScreen currentScreen = event.getGui();
        if (currentScreen instanceof GuiCreateWorld && event.getButton().id == WORLD_TYPE_BUTTON_ID) {
            GuiCreateWorld createWorld = (GuiCreateWorld) currentScreen;
            TerrariumWorldType worldType = getSelectedWorldType(createWorld);
            if (worldType != null) {
                if (worldType.isHidden() && !GuiScreen.isShiftKeyDown()) {
                    ClientProxy.actionPerformed(currentScreen, event.getButton());
                } else {
                    hideStructuresButton(createWorld);
                }
            }
        }
    }

    @Nullable
    private static TerrariumWorldType getSelectedWorldType(GuiCreateWorld createWorld) {
        int selectedWorldIndex = ClientProxy.getSelectedWorldType(createWorld);
        return GenericWorldType.unwrapAs(WorldType.WORLD_TYPES[selectedWorldIndex], TerrariumWorldType.class);
    }

    private static void hideStructuresButton(GuiCreateWorld createWorld) {
        GuiButton structuresButton = ClientProxy.getStructuresButton(createWorld);
        if (structuresButton != null) {
            structuresButton.visible = false;
        }
    }
}
