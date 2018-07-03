package net.gegy1000.terrarium.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public abstract class ListGuiWidget extends GuiListExtended {
    public ListGuiWidget(Minecraft client, int screenWidth, int screenHeight, int x, int y, int width, int height, int slotHeight) {
        super(client, screenWidth, screenHeight, y, y + height, slotHeight);
        this.left = x;
        this.right = x + width;
    }

    @Override
    public int getSlotIndexFromScreenCoords(int posX, int posY) {
        int scrolledY = posY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
        int slotIndex = scrolledY / this.slotHeight;
        if (posX >= this.left && posX <= this.right && slotIndex >= 0 && scrolledY >= 0 && slotIndex < this.getSize() && posX < this.getScrollBarX()) {
            return slotIndex;
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent) {
        if (this.isMouseYWithinSlotBounds(mouseY)) {
            int slotIndex = this.getSlotIndexFromScreenCoords(mouseX, mouseY);

            if (slotIndex >= 0) {
                int minEntryX = this.left + 2;
                int minEntryY = this.top + 4 - this.getAmountScrolled() + slotIndex * this.slotHeight + this.headerPadding;
                int localX = mouseX - minEntryX;
                int localY = mouseY - minEntryY;

                if (this.getListEntry(slotIndex).mousePressed(slotIndex, mouseX, mouseY, mouseEvent, localX, localY)) {
                    this.setEnabled(false);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(int x, int y, int mouseEvent) {
        for (int i = 0; i < this.getSize(); ++i) {
            int minEntryX = this.left + 2;
            int minEntryY = this.top + 4 - this.getAmountScrolled() + i * this.slotHeight + this.headerPadding;
            int localX = x - minEntryX;
            int localY = y - minEntryY;
            this.getListEntry(i).mouseReleased(i, x, y, mouseEvent, localX, localY);
        }

        this.setEnabled(true);
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.mouseX = mouseX;
            this.mouseY = mouseY;

            this.drawBackground();

            int minBarX = this.getScrollBarX();
            int maxBarX = minBarX + 6;
            this.bindAmountScrolled();

            int minEntryX = this.left + 2;
            int minEntryY = this.top + 4 - MathHelper.floor(this.amountScrolled);

            GlStateManager.disableLighting();
            GlStateManager.disableFog();

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder builder = tessellator.getBuffer();
            this.drawContainerBackground(tessellator);

            if (this.hasListHeader) {
                this.drawListHeader(minEntryX, minEntryY, tessellator);
            }

            this.drawSelectionBox(minEntryX, minEntryY, mouseX, mouseY, partialTicks);
            GlStateManager.disableDepth();

            this.overlayBackground(0, this.top, 255, 255);
            this.overlayBackground(this.bottom, this.height, 255, 255);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            GlStateManager.disableAlpha();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableTexture2D();

            this.drawEdgeShadow(tessellator, builder);
            this.drawScrollbar(minBarX, maxBarX, tessellator, builder);
            this.renderDecorations(mouseX, mouseY);

            GlStateManager.enableTexture2D();
            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
        }
    }

    private void drawEdgeShadow(Tessellator tessellator, BufferBuilder builder) {
        int shadowSize = 4;

        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        builder.pos(this.left, this.top + shadowSize, 0.0).tex(0.0, 1.0).color(0, 0, 0, 0).endVertex();
        builder.pos(this.right, this.top + shadowSize, 0.0).tex(1.0, 1.0).color(0, 0, 0, 0).endVertex();
        builder.pos(this.right, this.top, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
        builder.pos(this.left, this.top, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
        tessellator.draw();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        builder.pos(this.left, this.bottom, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
        builder.pos(this.right, this.bottom, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
        builder.pos(this.right, this.bottom - shadowSize, 0.0).tex(1.0, 0.0).color(0, 0, 0, 0).endVertex();
        builder.pos(this.left, this.bottom - shadowSize, 0.0).tex(0.0, 0.0).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
    }

    private void drawScrollbar(int minBarX, int maxBarX, Tessellator tessellator, BufferBuilder builder) {
        int maxScroll = this.getMaxScroll();

        if (maxScroll > 0) {
            int height = this.bottom - this.top;
            int barY = this.getBarY();
            int barSize = Math.max((int) this.amountScrolled * (height - barY) / maxScroll + this.top, this.top);

            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            builder.pos(minBarX, this.bottom, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
            builder.pos(maxBarX, this.bottom, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
            builder.pos(maxBarX, this.top, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
            builder.pos(minBarX, this.top, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
            tessellator.draw();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            builder.pos(minBarX, barSize + barY, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
            builder.pos(maxBarX, barSize + barY, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
            builder.pos(maxBarX, barSize, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
            builder.pos(minBarX, barSize, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
            tessellator.draw();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            builder.pos(minBarX, barSize + barY - 1, 0.0).tex(0.0, 1.0).color(192, 192, 192, 255).endVertex();
            builder.pos(maxBarX - 1, barSize + barY - 1, 0.0).tex(1.0, 1.0).color(192, 192, 192, 255).endVertex();
            builder.pos(maxBarX - 1, barSize, 0.0).tex(1.0, 0.0).color(192, 192, 192, 255).endVertex();
            builder.pos(minBarX, barSize, 0.0).tex(0.0, 0.0).color(192, 192, 192, 255).endVertex();
            tessellator.draw();
        }
    }

    @Override
    protected void drawSelectionBox(int minEntryX, int minEntryY, int mouseXIn, int mouseYIn, float partialTicks) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuffer();

        for (int slotIndex = 0; slotIndex < this.getSize(); ++slotIndex) {
            int entryY = minEntryY + slotIndex * this.slotHeight + this.headerPadding;
            int entryHeight = this.slotHeight - 4;

            if (entryY > this.bottom || entryY + entryHeight < this.top) {
                this.updateItemPos(slotIndex, minEntryX, entryY, partialTicks);
            }

            if (this.showSelectionBox && this.isSelected(slotIndex)) {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.disableTexture2D();
                builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                builder.pos(this.left, entryY + entryHeight + 2, 0.0).tex(0.0, 1.0).color(128, 128, 128, 255).endVertex();
                builder.pos(this.right, entryY + entryHeight + 2, 0.0).tex(1.0, 1.0).color(128, 128, 128, 255).endVertex();
                builder.pos(this.right, entryY - 2, 0.0).tex(1.0, 0.0).color(128, 128, 128, 255).endVertex();
                builder.pos(this.left, entryY - 2, 0.0).tex(0.0, 0.0).color(128, 128, 128, 255).endVertex();
                builder.pos(this.left + 1, entryY + entryHeight + 1, 0.0).tex(0.0, 1.0).color(0, 0, 0, 255).endVertex();
                builder.pos(this.right - 1, entryY + entryHeight + 1, 0.0).tex(1.0, 1.0).color(0, 0, 0, 255).endVertex();
                builder.pos(this.right - 1, entryY - 1, 0.0).tex(1.0, 0.0).color(0, 0, 0, 255).endVertex();
                builder.pos(this.left + 1, entryY - 1, 0.0).tex(0.0, 0.0).color(0, 0, 0, 255).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
            }

            this.drawSlot(slotIndex, minEntryX, entryY, entryHeight, mouseXIn, mouseYIn, partialTicks);
        }
    }

    @Override
    public void handleMouseInput() {
        if (this.isMouseYWithinSlotBounds(this.mouseY)) {
            int localY = this.mouseY - this.top - this.headerPadding + (int) this.amountScrolled - 4;
            int clickedSlot = localY / this.slotHeight;

            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && this.mouseY >= this.top && this.mouseY <= this.bottom) {
                this.handleSlotClick(localY, clickedSlot, false);
            }

            if (Mouse.isButtonDown(0) && this.getEnabled()) {
                if (this.initialClickY == -1) {
                    boolean shouldScroll = true;

                    if (this.mouseY >= this.top && this.mouseY <= this.bottom) {
                        if (!this.handleSlotClick(localY, clickedSlot, true)) {
                            shouldScroll = false;
                        }

                        int minBarX = this.getScrollBarX();
                        int maxBarX = minBarX + 6;

                        if (this.mouseX >= minBarX && this.mouseX <= maxBarX) {
                            float maxScroll = Math.max(this.getMaxScroll(), 1);
                            int barY = this.getBarY();

                            this.scrollMultiplier = -1.0F / ((this.bottom - this.top - barY) / maxScroll);
                        } else {
                            this.scrollMultiplier = 1.0F;
                        }

                        this.initialClickY = shouldScroll ? this.mouseY : -2;
                    } else {
                        this.initialClickY = -2;
                    }
                } else if (this.initialClickY >= 0) {
                    this.amountScrolled -= (float) (this.mouseY - this.initialClickY) * this.scrollMultiplier;
                    this.initialClickY = this.mouseY;
                }
            } else {
                this.initialClickY = -1;
            }

            int deltaWheel = Mouse.getEventDWheel();
            if (deltaWheel != 0) {
                this.amountScrolled -= MathHelper.clamp(deltaWheel, -1, 1) * this.slotHeight / 2.0F;
            }
        }
    }

    private boolean handleSlotClick(int localY, int clickedSlot, boolean scroll) {
        if (clickedSlot < this.getSize() && this.mouseX >= this.left && this.mouseX <= this.right && clickedSlot >= 0 && localY >= 0) {
            boolean doubleClicked = scroll && clickedSlot == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L;
            this.elementClicked(clickedSlot, doubleClicked, this.mouseX, this.mouseY);
            this.selectedElement = clickedSlot;
            if (scroll) {
                this.lastClicked = Minecraft.getSystemTime();
            }
        } else if (this.mouseX >= this.left && this.mouseX <= this.right && localY < 0) {
            this.clickedHeader(this.mouseX - this.left, this.mouseY - this.top + (int) this.amountScrolled - 4);
            return false;
        }
        return true;
    }

    private int getBarY() {
        int height = this.bottom - this.top;
        return MathHelper.clamp(height * height / this.getContentHeight(), 32, height - 8);
    }

    @Override
    public int getListWidth() {
        return this.right - this.left;
    }

    @Override
    protected int getScrollBarX() {
        return this.right - 6;
    }
}
