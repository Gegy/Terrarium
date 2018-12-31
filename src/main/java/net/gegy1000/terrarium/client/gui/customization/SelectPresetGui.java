package net.gegy1000.terrarium.client.gui.customization;

import net.gegy1000.terrarium.client.gui.widget.PresetList;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.customization.TerrariumPreset;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;

public class SelectPresetGui extends Gui {
    private static final int SELECT_BUTTON = 0;
    private static final int CANCEL_BUTTON = 1;

    private final TerrariumCustomizationGui parent;
    private final TerrariumGeneratorType<?> worldType;

    private ButtonWidget selectButton;

    private PresetList presetList;
    private TerrariumPreset selectedPreset;

    public SelectPresetGui(TerrariumCustomizationGui parent, TerrariumGeneratorType<?> worldType) {
        this.parent = parent;
        this.worldType = worldType;
    }

    @Override
    public void onInitialized() {
        this.presetList = new PresetList(this.client, this, this.worldType);
        this.listeners.add(this.presetList);

        this.selectButton = this.addButton(new ButtonWidget(SELECT_BUTTON, this.width / 2 - 154, this.height - 28, 150, 20, I18n.translate("gui.done")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                SelectPresetGui.this.applyPreset();
            }
        });
        this.addButton(new ButtonWidget(CANCEL_BUTTON, this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("gui.cancel")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                SelectPresetGui.this.client.openGui(SelectPresetGui.this.parent);
            }
        });

        this.selectButton.enabled = false;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.drawBackground();
        this.presetList.draw(mouseX, mouseY, partialTicks);
        this.drawStringCentered(this.fontRenderer, I18n.translate("gui.terrarium.select_preset.name"), this.width / 2, 20, 0xFFFFFF);
        super.draw(mouseX, mouseY, partialTicks);
    }

    public void selectPreset(TerrariumPreset preset) {
        this.selectedPreset = preset;
        this.selectButton.enabled = preset != null;
    }

    public void applyPreset() {
        if (this.selectedPreset != null) {
            this.parent.applyPreset(this.selectedPreset);
        }
        this.client.openGui(this.parent);
    }
}
