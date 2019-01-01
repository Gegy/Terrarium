package net.gegy1000.terrarium.client.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public abstract class ListGuiWidget<E extends EntryListWidget.Entry<E>> extends EntryListWidget<E> {
    public ListGuiWidget(MinecraftClient client, int screenWidth, int screenHeight, int x, int y, int width, int height, int slotHeight) {
        super(client, screenWidth, screenHeight, y, y + height, slotHeight);
        this.x1 = x;
        this.x2 = x + width;
    }

//    @Override
//    public int getSlotIndexFromScreenCoords(int posX, int posY) {
//        int scrolledY = posY - this.y1 - this.field_2174 + (int) this.amountScrolled - 4;
//        int slotIndex = scrolledY / this.slotHeight;
//        if (posX >= this.x1 && posX <= this.x2 && slotIndex >= 0 && scrolledY >= 0 && slotIndex < this.getSize() && posX < this.getScrollBarX()) {
//            return slotIndex;
//        }
//        return -1;
//    }
//
//    @Override
//    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
//        if (this.isMouseYWithinSlotBounds(mouseY)) {
//            int slotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);
//
//            if (slotIndex >= 0) {
//                int minEntryX = this.x1 + 2;
//                int minEntryY = this.y1 + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.field_2174;
//                int localX = mouseX - minEntryX;
//                int localY = mouseY - minEntryY;
//
//                if (this.getListEntry(slotIndex).mousePressed(slotIndex, mouseX, mouseY, mouseEvent, localX, localY)) {
//                    this.setEnabled(false);
//                    return true;
//                }
//            }
//        }
//
//        return false;
//    }
//
//    @Override
//    public boolean mouseReleased(int x, int y, int mouseEvent) {
//        for (int i = 0; i < this.getSize(); ++i) {
//            int minEntryX = this.x1 + 2;
//            int minEntryY = this.y1 + 4 - this.getAmountScrolled() + i * this.slotHeight + this.field_2174;
//            int localX = x - minEntryX;
//            int localY = y - minEntryY;
//            this.getEntries().get(i).mouseReleased(i, x, y, mouseEvent, localX, localY);
//        }
//
//        this.setEnabled(true);
//        return false;
//    }

    @Override
    public void draw(int mouseX, int mouseY, float delta) {
        if (this.visible) {
            this.drawBackground();

            int minBarX = this.getScrollbarPosition();
            int maxBarX = minBarX + 6;
            this.clampScrollY();

            int minEntryX = this.x1 + 2;
            int minEntryY = this.y1 + 4 - MathHelper.floor(this.scrollY);

            GlStateManager.disableLighting();
            GlStateManager.disableFog();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBufferBuilder();
            builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV_COLOR);
            builder.vertex(this.x1, this.y2, 0.0).texture(this.x1 / 32.0F, (this.y2 + this.scrollY) / 32.0F).color(32, 32, 32, 255).next();
            builder.vertex(this.x2, this.y2, 0.0).texture(this.x2 / 32.0F, (this.y2 + this.scrollY) / 32.0F).color(32, 32, 32, 255).next();
            builder.vertex(this.x2, this.y1, 0.0).texture(this.x2 / 32.0F, (this.y1 + this.scrollY) / 32.0F).color(32, 32, 32, 255).next();
            builder.vertex(this.x1, this.y1, 0.0).texture(this.x1 / 32.0F, (this.y1 + this.scrollY) / 32.0F).color(32, 32, 32, 255).next();
            tessellator.draw();

            // header
            if (this.field_2170) {
                this.method_1940(minEntryX, minEntryY, tessellator);
            }

            this.drawEntries(minEntryX, minEntryY, mouseX, mouseY, delta);
            GlStateManager.disableDepthTest();

            this.method_1954(0, this.y1, 255, 255);
            this.method_1954(this.y2, this.height, 255, 255);

            GlStateManager.enableBlend();
            GlStateManager.blendFuncSeparate(GlStateManager.SrcBlendFactor.SRC_ALPHA, GlStateManager.DstBlendFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcBlendFactor.ZERO, GlStateManager.DstBlendFactor.ONE);
            GlStateManager.disableAlphaTest();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableTexture();

            this.drawEdgeShadow(tessellator, builder);
            this.drawScrollbar(minBarX, maxBarX, tessellator, builder);
            this.method_1942(mouseX, mouseY);

            GlStateManager.enableTexture();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableAlphaTest();
            GlStateManager.disableBlend();
        }
    }

    private void drawEdgeShadow(Tessellator tessellator, BufferBuilder builder) {
        int shadowSize = 4;

        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV_COLOR);
        builder.vertex(this.x1, this.y1 + shadowSize, 0.0).texture(0.0, 1.0).color(0, 0, 0, 0).next();
        builder.vertex(this.x2, this.y1 + shadowSize, 0.0).texture(1.0, 1.0).color(0, 0, 0, 0).next();
        builder.vertex(this.x2, this.y1, 0.0).texture(1.0, 0.0).color(0, 0, 0, 255).next();
        builder.vertex(this.x1, this.y1, 0.0).texture(0.0, 0.0).color(0, 0, 0, 255).next();
        tessellator.draw();
        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV_COLOR);
        builder.vertex(this.x1, this.y2, 0.0).texture(0.0, 1.0).color(0, 0, 0, 255).next();
        builder.vertex(this.x2, this.y2, 0.0).texture(1.0, 1.0).color(0, 0, 0, 255).next();
        builder.vertex(this.x2, this.y2 - shadowSize, 0.0).texture(1.0, 0.0).color(0, 0, 0, 0).next();
        builder.vertex(this.x1, this.y2 - shadowSize, 0.0).texture(0.0, 0.0).color(0, 0, 0, 0).next();
        tessellator.draw();
    }

    private void drawScrollbar(int minBarX, int maxBarX, Tessellator tessellator, BufferBuilder builder) {
        int maxScroll = this.getMaxScrollY();

        if (maxScroll > 0) {
            int height = this.y2 - this.y1;
            int barY = this.getBarY();
            int barSize = Math.max((int) this.scrollY * (height - barY) / maxScroll + this.y1, this.y1);

            builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV_COLOR);
            builder.vertex(minBarX, this.y2, 0.0).texture(0.0, 1.0).color(0, 0, 0, 255).next();
            builder.vertex(maxBarX, this.y2, 0.0).texture(1.0, 1.0).color(0, 0, 0, 255).next();
            builder.vertex(maxBarX, this.y1, 0.0).texture(1.0, 0.0).color(0, 0, 0, 255).next();
            builder.vertex(minBarX, this.y1, 0.0).texture(0.0, 0.0).color(0, 0, 0, 255).next();
            tessellator.draw();
            builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV_COLOR);
            builder.vertex(minBarX, barSize + barY, 0.0).texture(0.0, 1.0).color(128, 128, 128, 255).next();
            builder.vertex(maxBarX, barSize + barY, 0.0).texture(1.0, 1.0).color(128, 128, 128, 255).next();
            builder.vertex(maxBarX, barSize, 0.0).texture(1.0, 0.0).color(128, 128, 128, 255).next();
            builder.vertex(minBarX, barSize, 0.0).texture(0.0, 0.0).color(128, 128, 128, 255).next();
            tessellator.draw();
            builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV_COLOR);
            builder.vertex(minBarX, barSize + barY - 1, 0.0).texture(0.0, 1.0).color(192, 192, 192, 255).next();
            builder.vertex(maxBarX - 1, barSize + barY - 1, 0.0).texture(1.0, 1.0).color(192, 192, 192, 255).next();
            builder.vertex(maxBarX - 1, barSize, 0.0).texture(1.0, 0.0).color(192, 192, 192, 255).next();
            builder.vertex(minBarX, barSize, 0.0).texture(0.0, 0.0).color(192, 192, 192, 255).next();
            tessellator.draw();
        }
    }

    @Override
    protected void drawEntries(int minEntryX, int minEntryY, int mouseX, int mouseY, float delta) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBufferBuilder();

        for (int slotIndex = 0; slotIndex < this.getEntryCount(); slotIndex++) {
            int entryY = minEntryY + slotIndex * this.entryHeight + this.field_2174;
            int entryHeight = this.entryHeight - 4;

            if (entryY > this.y2 || entryY + entryHeight < this.y1) {
                this.method_1952(slotIndex, minEntryX, entryY, delta);
            }

            if (this.field_2171 && this.isSelectedEntry(slotIndex)) {
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture();
                builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV_COLOR);
                builder.vertex(this.x1, entryY + entryHeight + 2, 0.0).texture(0.0, 1.0).color(128, 128, 128, 255).next();
                builder.vertex(this.x2, entryY + entryHeight + 2, 0.0).texture(1.0, 1.0).color(128, 128, 128, 255).next();
                builder.vertex(this.x2, entryY - 2, 0.0).texture(1.0, 0.0).color(128, 128, 128, 255).next();
                builder.vertex(this.x1, entryY - 2, 0.0).texture(0.0, 0.0).color(128, 128, 128, 255).next();
                builder.vertex(this.x1 + 1, entryY + entryHeight + 1, 0.0).texture(0.0, 1.0).color(0, 0, 0, 255).next();
                builder.vertex(this.x2 - 1, entryY + entryHeight + 1, 0.0).texture(1.0, 1.0).color(0, 0, 0, 255).next();
                builder.vertex(this.x2 - 1, entryY - 1, 0.0).texture(1.0, 0.0).color(0, 0, 0, 255).next();
                builder.vertex(this.x1 + 1, entryY - 1, 0.0).texture(0.0, 0.0).color(0, 0, 0, 255).next();
                tessellator.draw();
                GlStateManager.enableTexture();
            }

            this.drawEntry(slotIndex, minEntryX, entryY, entryHeight, mouseX, mouseY, delta);
        }
    }

    private int getBarY() {
        int height = this.y2 - this.y1;
        return MathHelper.clamp(height * height / this.getMaxScrollPosition(), 32, height - 8);
    }

    @Override
    public int getEntryWidth() {
        return this.x2 - this.x1;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x2 - 6;
    }
}
