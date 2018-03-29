package net.gegy1000.terrarium.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public abstract class ActionButtonWidget extends GuiButton {
    public ActionButtonWidget(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public ActionButtonWidget(int buttonId, int x, int y, int width, int height, String buttonText) {
        super(buttonId, x, y, width, height, buttonText);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.handlePress();
            return true;
        }
        return false;
    }

    protected abstract void handlePress();
}
