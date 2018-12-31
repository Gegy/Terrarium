package net.gegy1000.terrarium.client.preview;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class PreviewRenderer {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private final Gui gui;

    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public PreviewRenderer(Gui gui, double x, double y, double width, double height) {
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(WorldPreview preview, float zoom, float rotationX, float rotationY) {
        this.renderBackground();

        if (preview != null) {
            Window window = CLIENT.window;
            double scaleFactor = window.method_4495();

            GlStateManager.pushMatrix();
            GlStateManager.scaled(scaleFactor, scaleFactor, scaleFactor);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GuiRenderUtils.scissor(this.x, this.y, this.width, this.height);

            GlStateManager.enableRescaleNormal();
            GlStateManager.disableTexture();
            GlStateManager.enableDepthTest();

            GlStateManager.translated((this.x + this.gui.width) / 2.0 / scaleFactor, (this.y + this.height) / 2.0 / scaleFactor, 0.0);
            GlStateManager.scaled(zoom, -zoom, zoom);
            GlStateManager.rotatef(rotationX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(rotationY, 0.0F, 1.0F, 0.0F);

            GlStateManager.translated(0.0, -preview.getHeightOffset(), 0.0);

            GuiLighting.enableForItems();

            preview.renderChunks();

            GlStateManager.disableLighting();
            GlStateManager.disableDepthTest();
            GlStateManager.enableTexture();
            GlStateManager.disableRescaleNormal();

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            GlStateManager.popMatrix();
        }

        this.renderEdges();
    }

    private void renderBackground() {
        CLIENT.getTextureManager().bindTexture(Gui.OPTIONS_BG);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBufferBuilder();
        double tileSize = 32.0;
        GlStateManager.color4f(0.125F, 0.125F, 0.125F, 1.0F);
        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV);
        buffer.vertex(this.x, this.y + this.height, 0.0).texture(this.x / tileSize, (this.y + this.height) / tileSize).next();
        buffer.vertex(this.x + this.width, this.y + this.height, 0.0).texture((this.x + this.width) / tileSize, (this.y + this.height) / tileSize).next();
        buffer.vertex(this.x + this.width, this.y, 0.0).texture((this.x + this.width) / tileSize, this.y / tileSize).next();
        buffer.vertex(this.x, this.y, 0.0).texture(this.x / tileSize, this.y / tileSize).next();
        tessellator.draw();
    }

    private void renderEdges() {
        GlStateManager.disableTexture();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SrcBlendFactor.SRC_ALPHA, GlStateManager.DstBlendFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcBlendFactor.ZERO, GlStateManager.DstBlendFactor.ONE);
        GlStateManager.disableAlphaTest();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBufferBuilder();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(this.x, this.y + this.height, 0.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.x + this.width, this.y + this.height, 0.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.x + this.width, this.y + this.height - 4, 0.0).color(0, 0, 0, 0).next();
        buffer.vertex(this.x, this.y + this.height - 4, 0.0).color(0, 0, 0, 0).next();
        tessellator.draw();

        buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(this.x, this.y + 4, 0.0).color(0, 0, 0, 0).next();
        buffer.vertex(this.x + this.width, this.y + 4, 0.0).color(0, 0, 0, 0).next();
        buffer.vertex(this.x + this.width, this.y, 0.0).color(0, 0, 0, 255).next();
        buffer.vertex(this.x, this.y, 0.0).color(0, 0, 0, 255).next();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlphaTest();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture();
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }
}
