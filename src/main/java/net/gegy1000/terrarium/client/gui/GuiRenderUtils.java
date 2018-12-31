package net.gegy1000.terrarium.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GuiRenderUtils {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    public static void drawStringCentered(String text, int x, int y, int color) {
        CLIENT.fontRenderer.draw(text, x - CLIENT.fontRenderer.getStringWidth(text) / 2.0F, y, color);
    }

    public static void drawTooltip(List<String> lines, double mouseX, double mouseY) {
        Gui currentGui = CLIENT.currentGui;
        if (currentGui != null) {
            currentGui.drawTooltip(lines, (int) mouseX, (int) mouseY);
        }

        GlStateManager.disableLighting();
        GlStateManager.disableDepthTest();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void scissor(double x, double y, double width, double height) {
        Gui screen = CLIENT.currentGui;
        if (screen != null) {
            double scaleFactor = CLIENT.window.method_4495();
            GL11.glScissor((int) (x * scaleFactor), (int) ((screen.height - (y + height)) * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));
        }
    }
}
