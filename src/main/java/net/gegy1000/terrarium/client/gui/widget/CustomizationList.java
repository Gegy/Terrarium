package net.gegy1000.terrarium.client.gui.widget;

import net.gegy1000.terrarium.client.gui.customization.setting.CustomizationValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class CustomizationList extends GuiListExtended {
    private final Minecraft mc;
    private final GuiScreen parent;

    private final List<GuiButton> widgets = new ArrayList<>();

    private final List<DoubleWidgetEntry> entries = new ArrayList<>();

    public CustomizationList(Minecraft mc, GuiScreen parent) {
        super(mc, parent.width, parent.height, 25, parent.height / 2, 20);
        this.mc = mc;
        this.parent = parent;
    }

    public void addSlider(CustomizationValue<Double> value, double min, double max, double step, double fineStep) {
        this.widgets.add(new SliderWidget(0, 0, 0, value, min, max, step, fineStep));
    }

    public void addToggle(CustomizationValue<Boolean> value) {
        this.widgets.add(new ToggleWidget(0, 0, 0, value));
    }

    public void buildEntries() {
        this.entries.clear();
        for (int i = 0; i < this.widgets.size(); i += 2) {
            GuiButton primary = this.widgets.get(i);
            GuiButton secondary = i < this.widgets.size() ? this.widgets.get(i + 1) : null;
            this.entries.add(new DoubleWidgetEntry(primary, secondary));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.isMouseYWithinSlotBounds(mouseY)) {
            for (DoubleWidgetEntry element : this.entries) {
                element.drawTooltip(mouseX, mouseY);
            }
        }
    }

    @Override
    public int getListWidth() {
        return this.parent.width;
    }

    @Override
    protected int getScrollBarX() {
        return this.width - 6;
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return this.entries.get(index);
    }

    @Override
    protected int getSize() {
        return this.entries.size();
    }

    public class DoubleWidgetEntry implements IGuiListEntry {
        private final GuiButton primary;
        private final GuiButton secondary;

        public DoubleWidgetEntry(GuiButton primary, GuiButton secondary) {
            this.primary = primary;
            this.secondary = secondary;
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            if (this.primary != null) {
                this.primary.width = listWidth / 2 - 6;
                this.primary.x = 4;
                this.primary.y = y;
                this.primary.drawButton(CustomizationList.this.mc, mouseX, mouseY, partialTicks);
            }

            if (this.secondary != null) {
                this.secondary.width = listWidth / 2 - 6;
                this.secondary.x = listWidth / 2 + 2;
                this.secondary.y = y;
                this.secondary.drawButton(CustomizationList.this.mc, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return (this.primary != null && this.primary.mousePressed(CustomizationList.this.mc, mouseX, mouseY))
                    || (this.secondary != null && this.secondary.mousePressed(CustomizationList.this.mc, mouseX, mouseY));
        }

        @Override
        public void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            if (this.primary != null) {
                this.primary.mouseReleased(mouseX, mouseY);
            }
            if (this.secondary != null) {
                this.secondary.mouseReleased(mouseX, mouseY);
            }
        }

        public void drawTooltip(int mouseX, int mouseY) {
            if (this.primary instanceof TooltipRenderer) {
                this.drawTooltip((TooltipRenderer) this.primary, mouseX, mouseY);
            }
            if (this.secondary instanceof TooltipRenderer) {
                this.drawTooltip((TooltipRenderer) this.secondary, mouseX, mouseY);
            }
        }

        private void drawTooltip(TooltipRenderer renderer, int mouseX, int mouseY) {
            int width = CustomizationList.this.parent.width;
            int height = CustomizationList.this.parent.height;
            renderer.renderTooltip(CustomizationList.this.mc, mouseX, mouseY, width, height);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
