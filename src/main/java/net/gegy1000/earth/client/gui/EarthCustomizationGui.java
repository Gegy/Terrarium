package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.server.world.EarthWorldType;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.WorldType;

public class EarthCustomizationGui extends TerrariumCustomizationGui {
    private static final int SPAWNPOINT_BUTTON = 4;

    public EarthCustomizationGui(GuiCreateWorld parent, WorldType worldType, TerrariumWorldType terrariumType) {
        super(parent, worldType, terrariumType);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.addButton(new GuiButton(SPAWNPOINT_BUTTON, this.width / 2 + 4, this.height - 52, 150, 20, I18n.format("gui.earth.spawnpoint")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled && button.visible) {
            if (button.id == SPAWNPOINT_BUTTON) {
                this.freeze = true;
                this.mc.displayGuiScreen(new SelectEarthSpawnpointGui(this));
            }
        }
        super.actionPerformed(button);
    }

    public void applySpawnpoint(double latitude, double longitude) {
        this.settings.setDouble(EarthWorldType.SPAWN_LATITUDE, latitude);
        this.settings.setDouble(EarthWorldType.SPAWN_LONGITUDE, longitude);
        this.rebuildState();
    }
}
