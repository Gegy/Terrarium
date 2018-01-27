package net.gegy1000.terrarium.client.gui.customization;

import net.gegy1000.terrarium.client.gui.customization.setting.BuildingsValue;
import net.gegy1000.terrarium.client.gui.customization.setting.CaveGenValue;
import net.gegy1000.terrarium.client.gui.customization.setting.CustomizationValue;
import net.gegy1000.terrarium.client.gui.customization.setting.DecorateValue;
import net.gegy1000.terrarium.client.gui.customization.setting.HeightOffsetValue;
import net.gegy1000.terrarium.client.gui.customization.setting.HeightScaleValue;
import net.gegy1000.terrarium.client.gui.customization.setting.MapFeaturesValue;
import net.gegy1000.terrarium.client.gui.customization.setting.ResourceGenerationValue;
import net.gegy1000.terrarium.client.gui.customization.setting.ScaleValue;
import net.gegy1000.terrarium.client.gui.customization.setting.ScatterValue;
import net.gegy1000.terrarium.client.gui.customization.setting.SettingPreset;
import net.gegy1000.terrarium.client.gui.customization.setting.StreetsValue;
import net.gegy1000.terrarium.client.gui.widget.CustomizationList;
import net.gegy1000.terrarium.client.preview.PreviewController;
import net.gegy1000.terrarium.client.preview.PreviewRenderer;
import net.gegy1000.terrarium.client.preview.WorldPreview;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class CustomizeEarthGui extends GuiScreen {
    private static final int CANCEL_BUTTON = 0;
    private static final int DONE_BUTTON = 1;
    private static final int PREVIEW_BUTTON = 2;

    private static final int PRESET_BUTTON = 3;
    private static final int SPAWNPOINT_BUTTON = 4;

    private static final int PADDING_Y = 36;

    private final GuiCreateWorld parent;
    private final EarthGenerationSettings settings;

    private final CustomizationValue<Double> scaleValue;
    private final CustomizationValue<Double> heightScaleValue;
    private final CustomizationValue<Double> scatterValue;
    private final CustomizationValue<Double> heightOffsetValue;

    private final CustomizationValue<Boolean> decorateValue;
    private final CustomizationValue<Boolean> resourceGenerationValue;

    private final CustomizationValue<Boolean> buildingsValue;
    private final CustomizationValue<Boolean> streetsValue;

    private final CustomizationValue<Boolean> caveGenValue;
    private final CustomizationValue<Boolean> mapFeaturesValue;

    private CustomizationList customizationList;

    private PreviewRenderer renderer;
    private PreviewController controller;

    private WorldPreview preview = null;

    private boolean freeze;

    public CustomizeEarthGui(GuiCreateWorld parent) {
        this.parent = parent;
        this.settings = EarthGenerationSettings.deserialize(parent.chunkProviderSettingsJson);

        this.scaleValue = new ScaleValue(this.settings, this::rebuildState);
        this.heightScaleValue = new HeightScaleValue(this.settings, this::rebuildState);
        this.scatterValue = new ScatterValue(this.settings, this::rebuildState);
        this.heightOffsetValue = new HeightOffsetValue(this.settings, this::rebuildState);

        this.buildingsValue = new BuildingsValue(this.settings, this::rebuildState);
        this.streetsValue = new StreetsValue(this.settings, this::rebuildState);

        this.decorateValue = new DecorateValue(this.settings, this::rebuildState);
        this.resourceGenerationValue = new ResourceGenerationValue(this.settings, this::rebuildState);

        this.mapFeaturesValue = new MapFeaturesValue(this.settings, this::rebuildState);
        this.caveGenValue = new CaveGenValue(this.settings, this::rebuildState);
    }

    @Override
    public void initGui() {
        int previewWidth = this.width;
        int previewHeight = this.height / 2 - PADDING_Y * 2;
        int previewX = 0;
        int previewY = this.height - previewHeight - PADDING_Y;
        this.renderer = new PreviewRenderer(this, previewX, previewY, previewWidth, previewHeight);
        this.controller = new PreviewController(this.renderer, 0.3F, 1.0F);

        this.buttonList.clear();
        this.addButton(new GuiButton(CANCEL_BUTTON, this.width / 2 - 155, this.height - 28, 150, 20, I18n.translateToLocal("gui.cancel")));
        this.addButton(new GuiButton(DONE_BUTTON, this.width / 2 + 5, this.height - 28, 150, 20, I18n.translateToLocal("gui.done")));
        this.addButton(new GuiButton(PREVIEW_BUTTON, previewX + previewWidth - 20, previewY, 20, 20, "..."));

        this.addButton(new GuiButton(PRESET_BUTTON, 4, this.height / 2 + 8, this.width / 2 - 6, 20, I18n.translateToLocal("gui.terrarium.preset")));
        this.addButton(new GuiButton(SPAWNPOINT_BUTTON, this.width / 2 + 2, this.height / 2 + 8, this.width / 2 - 6, 20, I18n.translateToLocal("gui.terrarium.spawnpoint")));

        this.customizationList = new CustomizationList(this.mc, this);
        this.customizationList.addSlider(this.scaleValue, 1.0, 200.0, 5.0, 1.0);
        this.customizationList.addSlider(this.heightScaleValue, 0.01, 4.0, 0.5, 0.1);
        this.customizationList.addSlider(this.scatterValue, 0, 32, 1, 1);
        this.customizationList.addSlider(this.heightOffsetValue, 0, 128, 1, 1);

        this.customizationList.addToggle(this.buildingsValue);
        this.customizationList.addToggle(this.streetsValue);

        this.customizationList.addToggle(this.decorateValue);
        this.customizationList.addToggle(this.resourceGenerationValue);
        this.customizationList.addToggle(this.mapFeaturesValue);
        this.customizationList.addToggle(this.caveGenValue);

        this.customizationList.buildEntries();

        if (!this.freeze) {
            this.rebuildState();
        }

        this.freeze = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled && button.visible) {
            switch (button.id) {
                case CANCEL_BUTTON:
                    this.mc.displayGuiScreen(this.parent);
                    break;
                case DONE_BUTTON:
                    this.parent.chunkProviderSettingsJson = this.settings.serialize();
                    this.mc.displayGuiScreen(this.parent);
                    break;
                case PREVIEW_BUTTON:
                    this.previewLarge();
                    break;
                case PRESET_BUTTON:
                    this.freeze = true;
                    this.mc.displayGuiScreen(new SelectPresetGui(this));
                    break;
                case SPAWNPOINT_BUTTON:
                    this.freeze = true;
                    this.mc.displayGuiScreen(new SelectSpawnpointGui(this));
                    break;
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        this.controller.update();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.controller.mouseClicked(mouseX, mouseY, mouseButton);
        this.customizationList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);

        this.controller.mouseReleased(mouseX, mouseY, mouseButton);
        this.customizationList.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);

        this.controller.mouseDragged(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        this.customizationList.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.controller.updateMouse(mouseX, mouseY);

        this.drawDefaultBackground();

        this.customizationList.drawScreen(mouseX, mouseY, partialTicks);

        String title = I18n.translateToLocal("options.terrarium.customize_earth_title.name");
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 4, 0xFFFFFF);

        float zoom = this.controller.getZoom(partialTicks);
        float rotationX = this.controller.getRotationX(partialTicks);
        float rotationY = this.controller.getRotationY(partialTicks);
        this.renderer.render(this.preview, zoom, rotationX, rotationY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        if (!this.freeze) {
            super.onGuiClosed();

            this.deletePreview();
        }
    }

    public void applyPreset(SettingPreset preset) {
        preset.apply(this.settings);
        this.rebuildState();
    }

    public void applySpawnpoint(double latitude, double longitude) {
        this.settings.spawnLatitude = latitude;
        this.settings.spawnLongitude = longitude;
        this.rebuildState();
    }

    private void rebuildState() {
        this.deletePreview();

        BufferBuilder[] builders = new BufferBuilder[8];
        for (int i = 0; i < builders.length; i++) {
            builders[i] = new BufferBuilder(0x4000);
        }
        this.preview = new WorldPreview(this.settings, builders);
    }

    private void previewLarge() {
        if (this.preview != null) {
            this.freeze = true;
            this.mc.displayGuiScreen(new PreviewEarthGui(this.preview, this));
        }
    }

    private void deletePreview() {
        WorldPreview preview = this.preview;
        if (preview != null) {
            preview.delete();
        }
    }

    public EarthGenerationSettings getSettings() {
        return this.settings;
    }
}
