package net.gegy1000.terrarium.client.gui.customization;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.widget.CustomizationList;
import net.gegy1000.terrarium.client.preview.PreviewController;
import net.gegy1000.terrarium.client.preview.PreviewRenderer;
import net.gegy1000.terrarium.client.preview.WorldPreview;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.customization.PropertyPrototype;
import net.gegy1000.terrarium.server.world.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.customization.widget.CustomizationCategory;
import net.gegy1000.terrarium.server.world.customization.widget.CustomizationWidget;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.menu.NewLevelGui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.datafixers.NbtOps;
import net.minecraft.nbt.CompoundTag;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class TerrariumCustomizationGui extends Gui {
    protected static final int TOP_OFFSET = 32;
    protected static final int BOTTOM_OFFSET = 64;
    protected static final int PADDING_X = 5;

    protected final NewLevelGui parent;

    protected final TerrariumGeneratorType<?> worldType;

    protected GenerationSettings settings;

    protected CustomizationList activeList;
    protected CustomizationList categoryList;

    protected PreviewRenderer renderer;
    protected PreviewController controller;

    protected boolean previewDirty = true;
    protected WorldPreview preview = null;

    protected boolean freeze;

    public TerrariumCustomizationGui(NewLevelGui parent, TerrariumGeneratorType<?> worldType, TerrariumPreset defaultPreset) {
        this.parent = parent;
        this.worldType = worldType;
        if (!defaultPreset.getWorldType().equals(worldType.getIdentifier())) {
            throw new IllegalArgumentException("Cannot customize world with preset of wrong world type");
        }
        PropertyPrototype prototype = worldType.buildPropertyPrototype();
        CompoundTag settings = parent.field_3200;
        if (settings == null || settings.isEmpty()) {
            this.setSettings(defaultPreset.createSettings(prototype));
        } else {
            try {
                this.setSettings(GenerationSettings.deserialize(prototype, new Dynamic<>(NbtOps.INSTANCE, settings)));
            } catch (JsonSyntaxException e) {
                Terrarium.LOGGER.error("Failed to deserialize settings: {}", settings, e);
            }
        }
    }

    @Override
    public void onInitialized() {
        int previewWidth = this.width / 2 - PADDING_X * 2;
        int previewHeight = this.height - TOP_OFFSET - BOTTOM_OFFSET;
        int previewX = this.width / 2 + PADDING_X;
        int previewY = TOP_OFFSET;

        this.renderer = new PreviewRenderer(this, this.width / 2.0F + PADDING_X, previewY, previewWidth, previewHeight);
        this.controller = new PreviewController(this.renderer, 0.3F, 1.0F);

        this.addButton(new ButtonWidget(0, this.width / 2 - 154, this.height - 28, 150, 20, I18n.translate("gui.done")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                TerrariumCustomizationGui.this.parent.field_3200 = (CompoundTag) TerrariumCustomizationGui.this.settings.serialize(NbtOps.INSTANCE).getValue();
                TerrariumCustomizationGui.this.client.openGui(TerrariumCustomizationGui.this.parent);
            }
        });
        this.addButton(new ButtonWidget(1, this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("gui.cancel")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                TerrariumCustomizationGui.this.client.openGui(TerrariumCustomizationGui.this.parent);
            }
        });
        this.addButton(new ButtonWidget(2, previewX + previewWidth - 20, previewY, 20, 20, "...") {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                TerrariumCustomizationGui.this.previewLarge();
            }
        });

        this.addButton(new ButtonWidget(3, this.width / 2 - 154, this.height - 52, 150, 20, I18n.translate("gui.terrarium.preset")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                TerrariumCustomizationGui.this.freeze = true;
                TerrariumCustomizationGui.this.client.openGui(new SelectPresetGui(TerrariumCustomizationGui.this, TerrariumCustomizationGui.this.worldType));
            }
        });

        this.listeners.add(this.controller);

        ButtonWidget upLevelButton = new ButtonWidget(0, 0, 0, "<<") {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                TerrariumCustomizationGui.this.updateActiveList(TerrariumCustomizationGui.this.categoryList);
            }
        };

        Runnable onPropertyChange = () -> this.previewDirty = true;

        List<ButtonWidget> categoryListWidgets = new ArrayList<>();

        Collection<CustomizationCategory> categories = this.worldType.getCustomization().getCategories();
        for (CustomizationCategory category : categories) {
            List<ButtonWidget> currentWidgets = new ArrayList<>();
            currentWidgets.add(upLevelButton);
            for (CustomizationWidget widget : category.getWidgets()) {
                currentWidgets.add(widget.createWidget(this.settings, 0, 0, 0, onPropertyChange));
            }

            CustomizationList currentList = new CustomizationList(this.client, this, PADDING_X, TOP_OFFSET, previewWidth, previewHeight, currentWidgets);

            categoryListWidgets.add(new ButtonWidget(0, 0, 0, category.getLocalizedName()) {
                @Override
                public void onPressed(double mouseX, double mouseY) {
                    TerrariumCustomizationGui.this.activeList = currentList;
                }
            });
        }

        this.categoryList = new CustomizationList(this.client, this, PADDING_X, TOP_OFFSET, previewWidth, previewHeight, categoryListWidgets);
        this.updateActiveList(this.categoryList);

        if (!this.freeze) {
            if (this.preview != null) {
                this.preview.delete();
                this.preview = null;
            }
            this.previewDirty = true;
        }

        this.freeze = false;
    }

    protected void updateActiveList(CustomizationList list) {
        if (this.activeList != null) {
            this.listeners.remove(list);
        }
        this.activeList = list;
        this.listeners.add(list);
    }

    @Override
    public void update() {
        super.update();

        if (this.previewDirty) {
            this.rebuildState();
            this.previewDirty = false;
        }

        this.controller.update();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.openGui(this.parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        this.drawBackground();

        float zoom = this.controller.getZoom(partialTicks);
        float rotationX = this.controller.getRotationX(partialTicks);
        float rotationY = this.controller.getRotationY(partialTicks);
        this.renderer.render(this.preview, zoom, rotationX, rotationY);

        this.activeList.draw(mouseX, mouseY, partialTicks);

        String title = I18n.translate("options.terrarium.customize_world_title.name");
        this.drawStringCentered(this.fontRenderer, title, this.width / 2, 20, 0xFFFFFF);

        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClosed() {
        if (!this.freeze) {
            super.onClosed();

            this.deletePreview();
        }
    }

    public void applyPreset(TerrariumPreset preset) {
        this.setSettings(preset.createSettings(this.worldType.buildPropertyPrototype()));
        this.previewDirty = true;
    }

    protected void rebuildState() {
        this.deletePreview();

        BufferBuilder[] builders = new BufferBuilder[8];
        for (int i = 0; i < builders.length; i++) {
            builders[i] = new BufferBuilder(0x4000);
        }
        this.preview = new WorldPreview(this.worldType.getGenerator(), this.settings, builders);
    }

    private void previewLarge() {
        if (this.preview != null) {
            this.freeze = true;
            String settingsString = new Gson().toJson(this.settings.serialize(JsonOps.INSTANCE).getValue());
            this.client.openGui(new PreviewWorldGui(this.preview, this, settingsString));
        }
    }

    private void deletePreview() {
        WorldPreview preview = this.preview;
        if (preview != null) {
            preview.delete();
        }
    }

    protected void setSettings(GenerationSettings settings) {
        this.settings = settings;
    }

    public GenerationSettings getSettings() {
        return this.settings;
    }
}
