package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.customization.SelectPresetGui;
import net.gegy1000.terrarium.server.world.TerrariumGeneratorType;
import net.gegy1000.terrarium.server.world.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.customization.TerrariumPresetRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.text.TextFormat;
import net.minecraft.util.Identifier;
import net.minecraft.util.SystemUtil;

@Environment(EnvType.CLIENT)
public class PresetList extends EntryListWidget<PresetList.PresetEntry> {
    private static final Identifier FALLBACK_ICON = new Identifier(Terrarium.MODID, "textures/preset/fallback.png");
    private static final Identifier ICON_OVERLAY = new Identifier("textures/gui/world_selection.png");

    private final SelectPresetGui parent;

    private final ImmutableList<TerrariumPreset> presets;

    private int selectedIndex = -1;

    public PresetList(MinecraftClient client, SelectPresetGui parent, TerrariumGeneratorType worldType) {
        super(client, parent.width, parent.height, 32, parent.height - 64, 36);
        this.parent = parent;

        this.presets = ImmutableList.copyOf(TerrariumPresetRegistry.INSTANCE.getPresets());

        for (TerrariumPreset preset : this.presets) {
            if (preset.getWorldType().equals(worldType.getIdentifier())) {
                this.addEntry(new PresetEntry(client, preset));
            }
        }
    }

    public void selectPreset(int index) {
        this.parent.selectPreset(this.presets.get(index));
        this.selectedIndex = index;
    }

    public void applyPreset() {
        this.parent.applyPreset();
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    @Override
    public int getEntryWidth() {
        return super.getEntryWidth() + 50;
    }

    @Override
    protected boolean isSelectedEntry(int index) {
        return index == this.selectedIndex;
    }

    public class PresetEntry extends EntryListWidget.Entry<PresetEntry> {
        private final MinecraftClient client;

        private final TerrariumPreset preset;
        private final Identifier icon;

        private long lastClickTime;

        public PresetEntry(MinecraftClient client, TerrariumPreset preset) {
            this.client = client;
            this.preset = preset;

            this.icon = preset.getIcon();
        }

        @Override
        public void draw(int width, int var2, int mouseX, int mouseY, boolean selected, float delta) {
            int x = this.getX();
            int y = this.getY();

            this.client.fontRenderer.draw(this.preset.getLocalizedName(), x + 32 + 3, y + 1, 0xFFFFFF);
            String description = TextFormat.DARK_GRAY + this.preset.getLocalizedDescription();
            this.client.fontRenderer.draw(description, x + 32 + 3, y + this.client.fontRenderer.fontHeight + 3, 0xFFFFFF);

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            TextureManager textureManager = this.client.getTextureManager();
            textureManager.bindTexture(this.icon != null ? this.icon : FALLBACK_ICON);

            Gui.drawTexturedRect(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);

            if (this.client.options.touchscreen || selected) {
                Gui.drawRect(x, y, x + 32, y + 32, 0xA0909090);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

                textureManager.bindTexture(ICON_OVERLAY);
                Gui.drawTexturedRect(x, y, 0.0F, mouseX - x < 32 ? 32.0F : 0.0F, 32, 32, 256.0F, 256.0F);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            PresetList.this.selectPreset(this.field_2143);

            if (mouseX - this.getX() < 32 || SystemUtil.getMeasuringTimeMili() - this.lastClickTime < 250) {
                PresetList.this.applyPreset();
                return true;
            }

            this.lastClickTime = SystemUtil.getMeasuringTimeMili();
            return false;
        }
    }
}
