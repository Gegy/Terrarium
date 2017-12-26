package net.gegy1000.terrarium.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface TooltipRenderer {
    void renderTooltip(Minecraft mc, int mouseX, int mouseY, int width, int height);
}
