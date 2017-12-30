package net.gegy1000.terrarium.client.preview;

import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class PreviewRenderer {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private final GuiScreen gui;

    private final double x;
    private final double y;
    private final double width;
    private final double height;

    public PreviewRenderer(GuiScreen gui, double x, double y, double width, double height) {
        this.gui = gui;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void render(WorldPreview preview, float zoom, float rotationX, float rotationY) {
        this.renderBackground();

        if (preview != null) {
            ScaledResolution resolution = new ScaledResolution(MC);
            double scaleFactor = resolution.getScaleFactor();

            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GuiRenderUtils.scissor(this.x, this.y, this.width, this.height);

            GlStateManager.enableRescaleNormal();
            GlStateManager.disableTexture2D();
            GlStateManager.enableDepth();

            GlStateManager.translate(this.gui.width / scaleFactor / 2.0, (this.y + this.height / 2) / scaleFactor, 0.0);
            GlStateManager.scale(zoom, -zoom, zoom);
            GlStateManager.rotate(rotationX, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rotationY, 0.0F, 1.0F, 0.0F);

            GlStateManager.translate(0.0, -preview.getHeightOffset(), 0.0);

            RenderHelper.enableStandardItemLighting();

            preview.renderChunks();

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableRescaleNormal();

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            GlStateManager.popMatrix();
        }
    }

    private void renderBackground() {
        MC.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        double tileSize = 32.0;
        GlStateManager.color(0.125F, 0.125F, 0.125F, 1.0F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(this.x, this.y + this.height, 0.0).tex(this.x / tileSize, (this.y + this.height) / tileSize).endVertex();
        buffer.pos(this.x + this.width, this.y + this.height, 0.0).tex((this.x + this.width) / tileSize, (this.y + this.height) / tileSize).endVertex();
        buffer.pos(this.x + this.width, this.y, 0.0).tex((this.x + this.width) / tileSize, this.y / tileSize).endVertex();
        buffer.pos(this.x, this.y, 0.0).tex(this.x / tileSize, this.y / tileSize).endVertex();
        tessellator.draw();
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
