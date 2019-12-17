package net.gegy1000.terrarium.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiButtonExt;

public abstract class ActionButtonWidget extends GuiButtonExt {
    public ActionButtonWidget(String buttonText) {
        super(0, 0, 0, buttonText);
    }

    public ActionButtonWidget(int width, int height, String buttonText) {
        super(0, 0, 0, width, height, buttonText);
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
