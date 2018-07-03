package net.gegy1000.earth.client.gui.widget.map;

import net.gegy1000.earth.client.gui.widget.map.component.MapComponent;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SideOnly(Side.CLIENT)
public class SlippyMapWidget extends Gui {
    private static final Minecraft MC = Minecraft.getMinecraft();

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private final SlippyMap map;
    private final List<MapComponent> components = new ArrayList<>();

    private int prevMouseX;
    private int prevMouseY;

    private boolean mouseDown;
    private boolean mouseDragged;

    public SlippyMapWidget(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.map = new SlippyMap(width, height);
    }

    public SlippyMap getMap() {
        return this.map;
    }

    public void addComponent(MapComponent component) {
        this.components.add(component);
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.enableTexture2D();
        this.drawBackground();

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.x, this.y, 0.0F);

        ScaledResolution resolution = new ScaledResolution(MC);
        float scale = 1.0F / resolution.getScaleFactor();
        GlStateManager.scale(scale, scale, scale);

        int cameraX = this.map.getCameraX();
        int cameraY = this.map.getCameraY();
        int cameraZoom = this.map.getCameraZoom();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiRenderUtils.scissor(this.x + 4.0, this.y + 4.0, this.width - 8.0, this.height - 8.0);

        List<SlippyMapTilePos> tiles = this.map.getVisibleTiles();
        List<SlippyMapTilePos> cascadedTiles = this.map.cascadeTiles(tiles);
        cascadedTiles.sort(Comparator.comparingInt(SlippyMapTilePos::getZoom));

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();

        for (SlippyMapTilePos pos : cascadedTiles) {
            SlippyMapTile tile = this.map.getTile(pos);
            this.renderTile(cameraX, cameraY, cameraZoom, pos, tile, partialTicks);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        for (MapComponent component : this.components) {
            component.onDrawMap(this.map, resolution, mouseX - this.x, mouseY - this.y);
        }

        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        int scroll = Mouse.getDWheel();
        if (this.isSelected(mouseX, mouseY)) {
            this.map.zoom(MathHelper.clamp(scroll, -1, 1), mouseX - this.x, mouseY - this.y);
        }
    }

    private void renderTile(int cameraX, int cameraY, int cameraZoom, SlippyMapTilePos pos, SlippyMapTile image, float partialTicks) {
        image.update(partialTicks);

        if (image.getLocation() != null) {
            int deltaZoom = cameraZoom - pos.getZoom();
            double zoomScale = Math.pow(2.0, deltaZoom);
            int size = MathHelper.floor(SlippyMap.TILE_SIZE * zoomScale);
            int renderX = (pos.getX() << deltaZoom) * SlippyMap.TILE_SIZE - cameraX;
            int renderY = (pos.getY() << deltaZoom) * SlippyMap.TILE_SIZE - cameraY;

            MC.getTextureManager().bindTexture(image.getLocation());

            GlStateManager.color(1.0F, 1.0F, 1.0F, image.getTransition());
            Gui.drawScaledCustomSizeModalRect(renderX, renderY, 0, 0, SlippyMap.TILE_SIZE, SlippyMap.TILE_SIZE, size, size, SlippyMap.TILE_SIZE, SlippyMap.TILE_SIZE);
        }
    }

    private void drawBackground() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        MC.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float textureSize = 32.0F;
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(this.x + this.width, this.y, 0.0).tex((this.x + this.width) / textureSize, this.y / textureSize).color(32, 32, 32, 255).endVertex();
        buffer.pos(this.x, this.y, 0.0).tex(this.x / textureSize, this.y / textureSize).color(32, 32, 32, 255).endVertex();
        buffer.pos(this.x, (this.y + this.height), 0.0).tex(this.x / textureSize, (this.y + this.height) / textureSize).color(32, 32, 32, 255).endVertex();
        buffer.pos(this.x + this.width, this.y + this.height, 0.0).tex((this.x + this.width) / textureSize, (this.y + this.height) / textureSize).color(32, 32, 32, 255).endVertex();
        tessellator.draw();
    }

    public void mouseClicked(int mouseX, int mouseY) {
        this.prevMouseX = mouseX;
        this.prevMouseY = mouseY;

        if (this.isSelected(mouseX, mouseY)) {
            this.mouseDown = true;
        }
    }

    public void mouseDragged(int mouseX, int mouseY) {
        if (this.mouseDown) {
            int deltaX = this.prevMouseX - mouseX;
            int deltaY = this.prevMouseY - mouseY;

            this.map.drag(deltaX, deltaY);

            this.prevMouseX = mouseX;
            this.prevMouseY = mouseY;

            this.mouseDragged = true;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        if (this.mouseDown && !this.mouseDragged && this.isSelected(mouseX, mouseY)) {
            ScaledResolution resolution = new ScaledResolution(MC);

            for (MapComponent component : this.components) {
                component.onMapClicked(this.map, resolution, mouseX - this.x, mouseY - this.y);
            }
        }

        this.mouseDown = false;
        this.mouseDragged = false;
    }

    public void onGuiClosed() {
        this.map.shutdown();
    }

    private boolean isSelected(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX <= this.x + this.width && mouseY <= this.y + this.height;
    }
}
