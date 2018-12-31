package net.gegy1000.terrarium.client.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TooltipRenderer {
    void renderTooltip(int mouseX, int mouseY);
}
