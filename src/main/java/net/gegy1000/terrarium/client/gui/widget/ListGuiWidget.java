package net.gegy1000.terrarium.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;

@Environment(EnvType.CLIENT)
public abstract class ListGuiWidget<E extends EntryListWidget.Entry<E>> extends EntryListWidget<E> {
    public ListGuiWidget(MinecraftClient client, int screenWidth, int screenHeight, int x, int y, int width, int height, int slotHeight) {
        super(client, screenWidth, screenHeight, y, y + height, slotHeight);
        this.x1 = x;
        this.x2 = x + width;
    }

    @Override
    public int getEntryWidth() {
        return this.x2 - this.x1;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x2 - 6;
    }
}
