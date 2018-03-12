package net.gegy1000.terrarium.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class CustomizationList extends ListGuiWidget {
    private final Minecraft client;
    private final GuiScreen parent;

    private final List<SingleWidgetEntry> entries = new ArrayList<>();

    public CustomizationList(Minecraft client, GuiScreen parent, int x, int y, int width, int height, List<GuiButton> widgets) {
        super(client, parent.width, parent.height, x, y, width, height, 20);
        this.client = client;
        this.parent = parent;

        for (GuiButton widget : widgets) {
            this.entries.add(new SingleWidgetEntry(widget));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.isMouseYWithinSlotBounds(mouseY)) {
            for (SingleWidgetEntry element : this.entries) {
                element.drawTooltip(mouseX, mouseY);
            }
        }
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return this.entries.get(index);
    }

    @Override
    protected int getSize() {
        return this.entries.size();
    }

    public class SingleWidgetEntry implements IGuiListEntry {
        private final GuiButton button;

        public SingleWidgetEntry(GuiButton button) {
            this.button = button;
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {
        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            if (this.button != null) {
                this.button.width = listWidth - 10;
                this.button.x = CustomizationList.this.left + 5;
                this.button.y = y;
                this.button.drawButton(CustomizationList.this.client, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return this.button != null && this.button.mousePressed(CustomizationList.this.client, mouseX, mouseY);
        }

        @Override
        public void mouseReleased(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            if (this.button != null) {
                this.button.mouseReleased(mouseX, mouseY);
            }
        }

        public void drawTooltip(int mouseX, int mouseY) {
            if (this.button instanceof TooltipRenderer) {
                this.drawTooltip((TooltipRenderer) this.button, mouseX, mouseY);
            }
        }

        private void drawTooltip(TooltipRenderer renderer, int mouseX, int mouseY) {
            int width = CustomizationList.this.parent.width;
            int height = CustomizationList.this.parent.height;
            renderer.renderTooltip(CustomizationList.this.client, mouseX, mouseY, width, height);

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
