package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.server.world.EarthWorldDefinition;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.resources.I18n;

public class EarthCustomizationGui extends TerrariumCustomizationGui {
    private static final int SPAWNPOINT_BUTTON = 4;

    public EarthCustomizationGui(GuiCreateWorld parent, TerrariumWorldType worldType, TerrariumPreset defaultPreset) {
        super(parent, worldType, defaultPreset);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.addButton(new GuiButton(SPAWNPOINT_BUTTON, this.width / 2 + 4, this.height - 52, 150, 20, I18n.format("gui.earth.spawnpoint")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled && button.visible) {
            switch (button.id) {
                case SPAWNPOINT_BUTTON:
                    this.freeze = true;
                    this.mc.displayGuiScreen(new SelectEarthSpawnpointGui(this));
                    break;
            }
        }
        super.actionPerformed(button);
    }

    public void applySpawnpoint(double latitude, double longitude) {
        this.settings.setDouble(EarthWorldDefinition.SPAWN_LATITUDE, latitude);
        this.settings.setDouble(EarthWorldDefinition.SPAWN_LONGITUDE, longitude);
        this.rebuildState();
    }
}
