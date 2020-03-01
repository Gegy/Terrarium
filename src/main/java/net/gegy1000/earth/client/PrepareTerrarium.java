package net.gegy1000.earth.client;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.client.gui.SharedInitializingGui;
import net.gegy1000.earth.server.config.TerrariumEarthConfig;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.gengen.api.GenericWorldType;
import net.gegy1000.terrarium.client.ClientProxy;
import net.gegy1000.earth.client.gui.RemoteDataWarningGui;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Mod.EventBusSubscriber(modid = TerrariumEarth.ID, value = Side.CLIENT)
public final class PrepareTerrarium {
    private static Method actionPerformedMethod;

    static {
        try {
            actionPerformedMethod = ObfuscationReflectionHelper.findMethod(GuiScreen.class, "func_146284_a", void.class, GuiButton.class);
        } catch (UnableToFindMethodException e) {
            TerrariumEarth.LOGGER.warn("Failed to find actionPerformed method", e);
        }
    }

    public static GuiScreen prepareScreen(GuiScreen returnTo, Runnable onReturn) {
        GuiScreen screen = new SharedInitializingGui(returnTo, onReturn);
        if (!TerrariumEarthConfig.acceptedRemoteDataWarning) {
            screen = new RemoteDataWarningGui(screen);
        }
        return screen;
    }

    @SubscribeEvent
    public static void onButtonPress(GuiScreenEvent.ActionPerformedEvent event) {
        if (SharedEarthData.isInitialized()) {
            return;
        }

        GuiScreen gui = event.getGui();

        if (gui instanceof GuiCreateWorld && event.getButton().id == 0) {
            int selectedWorldIndex = ClientProxy.getSelectedWorldType((GuiCreateWorld) gui);
            TerrariumWorldType worldType = GenericWorldType.unwrapAs(WorldType.WORLD_TYPES[selectedWorldIndex], TerrariumWorldType.class);
            if (worldType != null) {
                onCreateWorldPressed(event.getButton(), gui);
                event.setCanceled(true);
            }
        }
    }

    private static void onCreateWorldPressed(GuiButton button, GuiScreen gui) {
        gui.mc.displayGuiScreen(new SharedInitializingGui(gui, () -> {
            gui.mc.displayGuiScreen(gui);
            if (actionPerformedMethod != null) {
                try {
                    actionPerformedMethod.invoke(gui, button);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    TerrariumEarth.LOGGER.warn("Failed to invoke actionPerformed", e);
                }
            }
        }));
    }

    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (SharedEarthData.isInitialized()) {
            return;
        }

        GuiScreen gui = event.getGui();

        if (gui instanceof GuiWorldSelection) {
            GuiListWorldSelection list = ((GuiWorldSelection) gui).selectionList;
            List<GuiListWorldSelectionEntry> entries = list.entries;
            for (int i = 0; i < entries.size(); i++) {
                GuiListWorldSelectionEntry entry = entries.get(i);
                entries.set(i, new HookedWorldSelectionEntry(list, entry));
            }
        }
    }

    private static class HookedWorldSelectionEntry extends GuiListWorldSelectionEntry {
        private static final Minecraft CLIENT = Minecraft.getMinecraft();
        private static final ISaveFormat SAVE_FORMAT = CLIENT.getSaveLoader();

        HookedWorldSelectionEntry(GuiListWorldSelection list, GuiListWorldSelectionEntry entry) {
            super(list, entry.worldSummary, SAVE_FORMAT);
        }

        @Override
        public void joinWorld() {
            if (!SharedEarthData.isInitialized()) {
                WorldInfo worldInfo = SAVE_FORMAT.getWorldInfo(this.worldSummary.getFileName());
                if (worldInfo != null) {
                    TerrariumWorldType worldType = GenericWorldType.unwrapAs(worldInfo.getTerrainType(), TerrariumWorldType.class);
                    if (worldType != null) {
                        CLIENT.displayGuiScreen(prepareScreen(CLIENT.currentScreen, super::joinWorld));
                        return;
                    }
                }
            }

            super.joinWorld();
        }
    }
}
