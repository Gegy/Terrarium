package net.gegy1000.earth.client.gui.widget.map;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.earth.client.gui.widget.map.component.MapComponent;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SlippyMapWidget extends Drawable implements GuiEventListener {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final String ATTRIBUTION = "\u00a9 OpenStreetMap Contributors";

    private final int x;
    private final int y;
    private final int width;
    private final int height;

    private final SlippyMap map;
    private final List<MapComponent> components = new ArrayList<>();

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
        GlStateManager.enableTexture();
        this.drawBackground();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(this.x, this.y, 0.0F);

        float scale = (float) (1.0F / CLIENT.window.method_4495());
        GlStateManager.scalef(scale, scale, scale);

        double cameraX = this.map.getCameraX();
        double cameraY = this.map.getCameraY();
        int cameraZoom = this.map.getCameraZoom();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GuiRenderUtils.scissor(this.x + 4.0, this.y + 4.0, this.width - 8.0, this.height - 8.0);

        Collection<SlippyMapTilePos> tiles = this.map.getVisibleTiles();
        List<SlippyMapTilePos> cascadedTiles = this.map.cascadeTiles(tiles);
        cascadedTiles.sort(Comparator.comparingInt(SlippyMapTilePos::getZoom));

        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();

        for (SlippyMapTilePos pos : cascadedTiles) {
            SlippyMapTile tile = this.map.getTile(pos);
            this.renderTile(cameraX, cameraY, cameraZoom, pos, tile, partialTicks);
        }

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        for (MapComponent component : this.components) {
            component.onDrawMap(this.map, mouseX - this.x, mouseY - this.y);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        int maxX = this.x + this.width - 4;
        int maxY = this.y + this.height - 4;

        FontRenderer fontRenderer = MinecraftClient.getInstance().fontRenderer;
        int attributionWidth = fontRenderer.getStringWidth(ATTRIBUTION) + 20;
        int attributionOriginX = maxX - attributionWidth;
        int attributionOriginY = maxY - fontRenderer.fontHeight - 4;
        drawRect(attributionOriginX, attributionOriginY, maxX, maxY, 0xC0101010);
        fontRenderer.draw(ATTRIBUTION, attributionOriginX + 10, attributionOriginY + fontRenderer.fontHeight / 2.0F - 1, 0xFFFFFFFF);

        GlStateManager.disableBlend();
        GlStateManager.disableAlphaTest();
    }

    private void renderTile(double cameraX, double cameraY, int cameraZoom, SlippyMapTilePos pos, SlippyMapTile image, float partialTicks) {
        image.update(partialTicks);

        if (image.getLocation() != null) {
            int deltaZoom = cameraZoom - pos.getZoom();
            double zoomScale = Math.pow(2.0, deltaZoom);
            int size = MathHelper.floor(SlippyMap.TILE_SIZE * zoomScale);
            int renderX = (pos.getX() << deltaZoom) * SlippyMap.TILE_SIZE - MathHelper.floor(cameraX);
            int renderY = (pos.getY() << deltaZoom) * SlippyMap.TILE_SIZE - MathHelper.floor(cameraY);

            CLIENT.getTextureManager().bindTexture(image.getLocation());

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, image.getTransition());
            Gui.drawTexturedRect(renderX, renderY, 0, 0, SlippyMap.TILE_SIZE, SlippyMap.TILE_SIZE, size, size, SlippyMap.TILE_SIZE, SlippyMap.TILE_SIZE);
        }
    }

    private void drawBackground() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBufferBuilder();
        CLIENT.getTextureManager().bindTexture(Gui.OPTIONS_BG);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float textureSize = 32.0F;
        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV_COLOR);
        builder.vertex(this.x + this.width, this.y, 0.0).texture((this.x + this.width) / textureSize, this.y / textureSize).color(32, 32, 32, 255).next();
        builder.vertex(this.x, this.y, 0.0).texture(this.x / textureSize, this.y / textureSize).color(32, 32, 32, 255).next();
        builder.vertex(this.x, (this.y + this.height), 0.0).texture(this.x / textureSize, (this.y + this.height) / textureSize).color(32, 32, 32, 255).next();
        builder.vertex(this.x + this.width, this.y + this.height, 0.0).texture((this.x + this.width) / textureSize, (this.y + this.height) / textureSize).color(32, 32, 32, 255).next();
        tessellator.draw();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.isSelected(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.map.drag(-deltaX, -deltaY);
        this.mouseDragged = true;

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!this.mouseDragged && this.isSelected(mouseX, mouseY)) {
            for (MapComponent component : this.components) {
                component.onMapClicked(this.map, mouseX - this.x, mouseY - this.y);
            }
            return true;
        }

        this.mouseDragged = false;
        return false;
    }

    @Override
    public boolean mouseScrolled(double scrollAmount) {
        double scale = CLIENT.window.method_4495();
        double mouseX = CLIENT.mouse.getX() / scale;
        double mouseY = CLIENT.mouse.getY() / scale;
        if (this.isSelected(mouseX, mouseY)) {
            int scrollSteps = MathHelper.floor(Math.signum(scrollAmount));
            this.map.zoom(scrollSteps, mouseX - this.x, mouseY - this.y);
            return true;
        }
        return false;
    }

    public void close() {
        this.map.shutdown();
    }

    private boolean isSelected(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseY >= this.y && mouseX <= this.x + this.width && mouseY <= this.y + this.height;
    }
}
