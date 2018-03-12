package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.ImmutableList;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.customization.SelectPresetGui;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPreset;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPresetRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PresetList extends GuiListExtended {
    private static final ResourceLocation FALLBACK_ICON = new ResourceLocation(Terrarium.MODID, "textures/preset/fallback.png");
    private static final ResourceLocation ICON_OVERLAY = new ResourceLocation("textures/gui/world_selection.png");

    private final SelectPresetGui parent;

    private final ImmutableList<TerrariumPreset> presets;
    private final ImmutableList<PresetEntry> entries;

    private int selectedIndex = -1;

    public PresetList(Minecraft mc, SelectPresetGui parent) {
        super(mc, parent.width, parent.height, 32, parent.height - 64, 36);
        this.parent = parent;

        this.presets = ImmutableList.copyOf(TerrariumPresetRegistry.getPresets());

        ImmutableList.Builder<PresetEntry> entryBuilder = ImmutableList.builder();
        for (TerrariumPreset preset : this.presets) {
            entryBuilder.add(new PresetEntry(mc, preset));
        }
        this.entries = entryBuilder.build();
    }

    public void selectPreset(int index) {
        this.parent.selectPreset(this.presets.get(index));
        this.selectedIndex = index;
    }

    public void applyPreset() {
        this.parent.applyPreset();
    }

    @Override
    public int getListWidth() {
        return super.getListWidth() + 50;
    }

    @Override
    protected int getScrollBarX() {
        return super.getScrollBarX() + 20;
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return this.entries.get(index);
    }

    @Override
    protected int getSize() {
        return this.entries.size();
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return slotIndex == this.selectedIndex;
    }

    public class PresetEntry implements IGuiListEntry {
        private final Minecraft mc;

        private final TerrariumPreset preset;
        private final ResourceLocation icon;

        private long lastClickTime;

        public PresetEntry(Minecraft mc, TerrariumPreset preset) {
            this.mc = mc;
            this.preset = preset;

            this.icon = preset.getIcon();
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            this.mc.fontRenderer.drawString(this.preset.getLocalizedName(), x + 32 + 3, y + 1, 0xFFFFFF);
            String description = TextFormatting.DARK_GRAY + this.preset.getLocalizedDescription();
            this.mc.fontRenderer.drawString(description, x + 32 + 3, y + this.mc.fontRenderer.FONT_HEIGHT + 3, 0xFFFFFF);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            TextureManager textureManager = this.mc.getTextureManager();
            textureManager.bindTexture(this.icon);
            if (textureManager.getTexture(this.icon) == TextureUtil.MISSING_TEXTURE) {
                textureManager.bindTexture(FALLBACK_ICON);
            }

            Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);

            if (this.mc.gameSettings.touchscreen || isSelected) {
                Gui.drawRect(x, y, x + 32, y + 32, 0xA0909090);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                textureManager.bindTexture(ICON_OVERLAY);
                Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, mouseX - x < 32 ? 32.0F : 0.0F, 32, 32, 256.0F, 256.0F);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            PresetList.this.selectPreset(slotIndex);

            if (relativeX < 32 || System.currentTimeMillis() - this.lastClickTime < 250) {
                PresetList.this.applyPreset();
                return true;
            }

            this.lastClickTime = System.currentTimeMillis();
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
        }
    }
}
