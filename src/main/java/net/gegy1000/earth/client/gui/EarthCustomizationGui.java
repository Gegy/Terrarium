package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.server.world.EarthGeneratorType;
import net.gegy1000.terrarium.client.gui.customization.TerrariumCustomizationGui;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.customization.TerrariumPreset;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class EarthCustomizationGui extends TerrariumCustomizationGui {
    private static final int SPAWNPOINT_BUTTON = 4;

    public EarthCustomizationGui(NewLevelGui parent, TerrariumGeneratorType worldType, TerrariumPreset defaultPreset) {
        super(parent, worldType, defaultPreset);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        this.addButton(new ButtonWidget(SPAWNPOINT_BUTTON, this.width / 2 + 4, this.height - 52, 150, 20, I18n.translate("gui.earth.spawnpoint")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                EarthCustomizationGui.this.freeze = true;
                EarthCustomizationGui.this.client.openGui(new SelectEarthSpawnpointGui(EarthCustomizationGui.this));
            }
        });
    }

    public void applySpawnpoint(double latitude, double longitude) {
        this.settings.setDouble(EarthGeneratorType.SPAWN_LATITUDE, latitude);
        this.settings.setDouble(EarthGeneratorType.SPAWN_LONGITUDE, longitude);
        this.rebuildState();
    }
}
