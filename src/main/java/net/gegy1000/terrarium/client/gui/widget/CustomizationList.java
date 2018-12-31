package net.gegy1000.terrarium.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomizationList extends ListGuiWidget<CustomizationList.SingleWidgetEntry> {
    public CustomizationList(MinecraftClient client, Gui parent, int x, int y, int width, int height, List<ButtonWidget> widgets) {
        super(client, parent.width, parent.height, x, y, width, height, 20);

        for (ButtonWidget widget : widgets) {
            this.addEntry(new SingleWidgetEntry(widget));
        }
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        super.draw(mouseX, mouseY, partialTicks);
        if (this.isSelected(mouseX, mouseY)) {
            for (SingleWidgetEntry entry : this.getEntries()) {
                entry.drawTooltip(mouseX, mouseY);
            }
        }
    }

    public class SingleWidgetEntry extends EntryListWidget.Entry<SingleWidgetEntry> {
        private final ButtonWidget button;

        public SingleWidgetEntry(ButtonWidget button) {
            this.button = button;
        }

        @Override
        public void draw(int var1, int var2, int mouseX, int mouseY, boolean selected, float delta) {
            if (this.button != null) {
                this.button.setWidth(CustomizationList.this.width - 10);
                this.button.x = CustomizationList.this.x1 + 5;
                this.button.y = this.getY();
                this.button.draw(mouseX, mouseY, delta);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.button != null && this.button.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.button != null && this.button.mouseReleased(mouseX, mouseY, button);
        }

        public void drawTooltip(int mouseX, int mouseY) {
            if (this.button instanceof TooltipRenderer) {
                this.drawTooltip((TooltipRenderer) this.button, mouseX, mouseY);
            }
        }

        private void drawTooltip(TooltipRenderer renderer, int mouseX, int mouseY) {
            renderer.renderTooltip(mouseX, mouseY);

            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
