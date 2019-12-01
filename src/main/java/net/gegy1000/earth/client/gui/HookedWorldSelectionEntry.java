package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.gengen.api.GenericWorldType;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListWorldSelection;
import net.minecraft.client.gui.GuiListWorldSelectionEntry;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;

public class HookedWorldSelectionEntry extends GuiListWorldSelectionEntry {
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
                GenericWorldType worldType = GenericWorldType.unwrap(worldInfo.getTerrainType());
                if (worldType instanceof TerrariumWorldType) {
                    CLIENT.displayGuiScreen(new SharedInitializingGui(CLIENT.currentScreen, super::joinWorld));
                    return;
                }
            }
        }

        super.joinWorld();
    }
}
