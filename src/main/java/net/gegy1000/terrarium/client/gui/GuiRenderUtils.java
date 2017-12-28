package net.gegy1000.terrarium.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiRenderUtils {
    private static final Minecraft MC = Minecraft.getMinecraft();

    public static void drawCenteredString(String text, int x, int y, int color) {
        MC.fontRenderer.drawString(text, x - MC.fontRenderer.getStringWidth(text) / 2, y, color);
    }

    public static void drawTooltip(List<String> lines, int mouseX, int mouseY) {
        ScaledResolution resolution = new ScaledResolution(MC);
        GuiUtils.drawHoveringText(lines, mouseX, mouseY, resolution.getScaledWidth(), resolution.getScaledHeight(), -1, MC.fontRenderer);

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
