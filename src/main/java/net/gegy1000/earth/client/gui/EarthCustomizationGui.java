package net.gegy1000.earth.client.gui;

import com.google.common.base.Strings;
import com.google.gson.JsonSyntaxException;
import net.gegy1000.earth.client.gui.preview.LargePreviewGui;
import net.gegy1000.earth.client.gui.preview.PreviewController;
import net.gegy1000.earth.client.gui.preview.PreviewRenderer;
import net.gegy1000.earth.client.gui.preview.WorldPreview;
import net.gegy1000.earth.server.world.EarthProperties;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.customization.SelectPresetGui;
import net.gegy1000.terrarium.client.gui.widget.ActionButtonWidget;
import net.gegy1000.terrarium.client.gui.widget.CustomizationList;
import net.gegy1000.terrarium.server.world.TerrariumWorldType;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.PropertySchema;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.generator.customization.widget.CustomizationCategory;
import net.gegy1000.terrarium.server.world.generator.customization.widget.CustomizationWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class EarthCustomizationGui extends GuiScreen {
    private static final long PREVIEW_UPDATE_INTERVAL = 200;

    private static final int DONE_BUTTON = 0;
    private static final int CANCEL_BUTTON = 1;

    private static final int PRESET_BUTTON = 2;

    private static final int PREVIEW_BUTTON = 3;
    private static final int SPAWNPOINT_BUTTON = 4;

    private static final int TOP_OFFSET = 32;
    private static final int BOTTOM_OFFSET = 64;
    private static final int PADDING_X = 5;

    private final GuiCreateWorld parent;

    private final TerrariumWorldType worldType;

    protected GenerationSettings settings;

    protected CustomizationList activeList;
    protected CustomizationList categoryList;

    private PreviewRenderer previewRenderer;
    private PreviewController previewController;

    private boolean previewDirty = true;
    private long lastPreviewUpdateTime;

    private final WorldPreview preview = new WorldPreview();

    public EarthCustomizationGui(GuiCreateWorld parent, TerrariumWorldType worldType) {
        this.parent = parent;
        this.worldType = worldType;

        PropertySchema schema = worldType.buildPropertySchema();
        String settingsString = parent.chunkProviderSettingsJson;
        if (!Strings.isNullOrEmpty(settingsString)) {
            try {
                this.setSettings(GenerationSettings.parse(schema, settingsString));
            } catch (JsonSyntaxException e) {
                Terrarium.LOGGER.error("Failed to parse settings: {}", settingsString, e);
            }
        }
    }

    @Override
    public void initGui() {
        int tabWidth = this.width / 2 - PADDING_X * 2;
        int tabHeight = this.height - TOP_OFFSET - BOTTOM_OFFSET;

        int previewX = this.width / 2 + PADDING_X;
        int previewY = TOP_OFFSET;

        int propertiesX = PADDING_X;
        int propertiesY = TOP_OFFSET;

        this.buttonList.clear();
        this.addButton(new GuiButton(DONE_BUTTON, this.width / 2 - 154, this.height - 28, 150, 20, I18n.format("gui.done")));
        this.addButton(new GuiButton(CANCEL_BUTTON, this.width / 2 + 4, this.height - 28, 150, 20, I18n.format("gui.cancel")));

        this.addButton(new GuiButton(PRESET_BUTTON, this.width / 2 - 154, this.height - 52, 150, 20, I18n.format("gui.terrarium.preset")));

        this.addButton(new GuiButton(PREVIEW_BUTTON, previewX + tabWidth - 20, previewY, 20, 20, "..."));
        this.addButton(new GuiButton(SPAWNPOINT_BUTTON, this.width / 2 + 4, this.height - 52, 150, 20, I18n.format("gui.earth.spawnpoint")));

        this.previewRenderer = new PreviewRenderer(this, this.width / 2.0F + PADDING_X, previewY, tabWidth, tabHeight);
        this.previewController = new PreviewController(this.previewRenderer, 0.3F, 1.0F);

        Runnable onPropertyChange = () -> this.previewDirty = true;

        ActionButtonWidget upLevelButton = new ActionButtonWidget("<<") {
            @Override
            protected void handlePress() {
                EarthCustomizationGui.this.activeList = EarthCustomizationGui.this.categoryList;
            }
        };

        List<GuiButton> categoryListWidgets = new ArrayList<>();

        Collection<CustomizationCategory> categories = this.worldType.getCustomization().getCategories();
        for (CustomizationCategory category : categories) {
            List<GuiButton> currentWidgets = new ArrayList<>();
            currentWidgets.add(upLevelButton);
            for (CustomizationWidget widget : category.getWidgets()) {
                try {
                    currentWidgets.add(widget.createWidget(this.settings, onPropertyChange));
                } catch (Throwable t) {
                    Terrarium.LOGGER.error("Failed to create widget for {}", widget, t);
                }
            }

            CustomizationList currentList = new CustomizationList(this.mc, this, propertiesX, propertiesY, tabWidth, tabHeight, currentWidgets);

            categoryListWidgets.add(new ActionButtonWidget(category.getLocalizedName()) {
                @Override
                protected void handlePress() {
                    EarthCustomizationGui.this.activeList = currentList;
                }
            });
        }

        this.categoryList = new CustomizationList(this.mc, this, propertiesX, propertiesY, tabWidth, tabHeight, categoryListWidgets);
        this.activeList = this.categoryList;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled && button.visible) {
            switch (button.id) {
                case DONE_BUTTON:
                    this.parent.chunkProviderSettingsJson = this.settings.serializeString();
                    this.mc.displayGuiScreen(this.parent);
                    break;
                case CANCEL_BUTTON:
                    this.mc.displayGuiScreen(this.parent);
                    break;
                case PRESET_BUTTON:
                    Consumer<TerrariumPreset> accept = preset -> {
                        this.applyPreset(preset);
                        this.mc.displayGuiScreen(this);
                    };
                    this.mc.displayGuiScreen(new SelectPresetGui(accept, this, this.worldType));
                    break;
                case SPAWNPOINT_BUTTON:
                    this.mc.displayGuiScreen(new SelectEarthSpawnpointGui(this));
                    break;
                case PREVIEW_BUTTON:
                    this.mc.displayGuiScreen(new LargePreviewGui(this.preview.retain(), this, this.settings.serializeString()));
                    break;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (this.activeList != null) {
            this.activeList.mouseClicked(mouseX, mouseY, mouseButton);
        }

        this.previewController.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);
        if (this.activeList != null) {
            this.activeList.mouseReleased(mouseX, mouseY, mouseButton);
        }

        this.previewController.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
        this.previewController.mouseDragged(mouseX, mouseY, mouseButton);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if (this.activeList != null) {
            this.activeList.handleMouseInput();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (this.previewDirty) {
            long time = System.currentTimeMillis();

            boolean previewExpired = time - this.lastPreviewUpdateTime > PREVIEW_UPDATE_INTERVAL;

            boolean generateInactive = this.preview.isGenerateInactive();
            if (previewExpired || generateInactive) {
                this.rebuildPreview();
                this.previewDirty = false;
                this.lastPreviewUpdateTime = time;
            }
        }

        this.previewController.update();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.previewController.updateMouse(mouseX, mouseY);

        this.drawDefaultBackground();

        this.drawPreview(partialTicks);

        if (this.activeList != null) {
            this.activeList.drawScreen(mouseX, mouseY, partialTicks);
        }

        String title = I18n.format("options.terrarium.customize_world_title.name");
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 20, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawPreview(float partialTicks) {
        float zoom = this.previewController.getZoom(partialTicks);
        float rotationX = this.previewController.getRotationX(partialTicks);
        float rotationY = this.previewController.getRotationY(partialTicks);
        this.previewRenderer.render(this.preview, zoom, rotationX, rotationY);
    }

    public void applyPreset(TerrariumPreset preset) {
        this.setSettings(preset.createProperties(this.worldType.buildPropertySchema()));
        this.previewDirty = true;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        this.preview.release();
    }

    public void applySpawnpoint(double latitude, double longitude) {
        this.settings.setDouble(EarthProperties.SPAWN_LATITUDE, latitude);
        this.settings.setDouble(EarthProperties.SPAWN_LONGITUDE, longitude);
        this.rebuildPreview();
    }

    private void rebuildPreview() {
        this.preview.rebuild(this.worldType, this.settings);
    }

    protected void setSettings(GenerationSettings settings) {
        this.settings = settings;
    }

    public GenerationSettings getSettings() {
        return this.settings;
    }
}
