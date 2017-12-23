package net.gegy1000.terrarium.client.gui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.client.preview.WorldPreview;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SideOnly(Side.CLIENT)
public class CustomizeEarthGui extends GuiScreen {
    private final GuiScreen parent;

    private final EarthGenerationSettings settings = new EarthGenerationSettings();

    private final ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("terrarium-preview-%d").build());
    private final BlockingQueue<BufferBuilder> builders = new ArrayBlockingQueue<>(8);

    private float rotationX = 45.0F;
    private float prevRotationX = this.rotationX;

    private float zoom = 0.3F;
    private float prevZoom = this.zoom;

    private int prevMouseX;
    private boolean mouseDown;

    private WorldPreview preview = null;

    public CustomizeEarthGui(GuiScreen parent) {
        this.parent = parent;

        for (int i = 0; i < 8; i++) {
            this.builders.add(new BufferBuilder(0x4000));
        }
    }

    @Override
    public void initGui() {
        this.rebuildState();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        this.prevRotationX = this.rotationX;
        this.prevZoom = this.zoom;

        int scroll = Mouse.getDWheel();
        this.zoom = MathHelper.clamp(this.zoom + scroll / 1600.0F, 0.3F, 1.0F);

        if (!this.mouseDown) {
            this.rotationX += 0.25F;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.mouseDown = true;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);

        this.mouseDown = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        this.rotationX += (mouseX - this.prevMouseX) * 0.5F;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.prevMouseX = mouseX;

        this.drawDefaultBackground();

        String title = I18n.translateToLocal("options.terrarium.customize_earth_title.name");
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 4, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawPreview(partialTicks);
    }

    private void drawPreview(float partialTicks) {
        double padding = 14.0;
        double width = this.width - padding * 2.0;
        double height = this.height / 2.0 - padding * 2.0;
        double x = padding;
        double y = this.height - height - padding;

        this.drawPreviewBackground(x, y, width, height);

        this.drawPreviewWorld(x, y, width, height, partialTicks);
    }

    private void drawPreviewWorld(double x, double y, double width, double height, float partialTicks) {
        WorldPreview preview = this.preview;

        if (preview != null) {
            ScaledResolution resolution = new ScaledResolution(this.mc);
            double scaleFactor = resolution.getScaleFactor();

            BlockPos centerPos = preview.getCenterBlockPos();

            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            this.scissor(x, y, width, height);

            GlStateManager.enableRescaleNormal();
            GlStateManager.disableTexture2D();
            GlStateManager.enableDepth();

            double scale = this.prevZoom + (this.zoom - this.prevZoom) * partialTicks;
            GlStateManager.translate(this.width / scaleFactor / 2.0, y / scaleFactor, 0.0);
            GlStateManager.scale(scale, -scale, scale);
            GlStateManager.rotate(15.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(this.prevRotationX + (this.rotationX - this.prevRotationX) * partialTicks, 0.0F, 1.0F, 0.0F);

            GlStateManager.translate(-centerPos.getX(), -preview.getHeightOffset(), -centerPos.getZ());

            RenderHelper.enableStandardItemLighting();

            preview.render();

            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableRescaleNormal();

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            GlStateManager.popMatrix();
        }
    }

    private void scissor(double x, double y, double width, double height) {
        double scaleFactor = new ScaledResolution(this.mc).getScaleFactor();
        GL11.glScissor((int) (x * scaleFactor), (int) ((this.height - (y + height)) * scaleFactor), (int) (width * scaleFactor), (int) (height * scaleFactor));
    }

    private void drawPreviewBackground(double x, double y, double width, double height) {
        this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        double tileSize = 32.0;
        GlStateManager.color(0.125F, 0.125F, 0.125F, 1.0F);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, 0.0).tex(x / tileSize, (y + height) / tileSize).endVertex();
        buffer.pos(x + width, y + height, 0.0).tex((x + width) / tileSize, (y + height) / tileSize).endVertex();
        buffer.pos(x + width, y, 0.0).tex((x + width) / tileSize, y / tileSize).endVertex();
        buffer.pos(x, y, 0.0).tex(x / tileSize, y / tileSize).endVertex();
        tessellator.draw();
    }

    private void rebuildState() {
        this.deletePreview();
        this.preview = new WorldPreview(this.settings, this.executor, this.builders);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        this.executor.shutdownNow();

        this.deletePreview();
    }

    private void deletePreview() {
        WorldPreview preview = this.preview;
        if (preview != null) {
            preview.delete();
        }
    }
}
