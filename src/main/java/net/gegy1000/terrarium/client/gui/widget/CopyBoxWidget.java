package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;

import java.util.List;

public class CopyBoxWidget extends Drawable implements GuiEventListener {
    private static final Identifier WIDGETS_TEXTURE = new Identifier(Terrarium.MODID, "textures/gui/widgets.png");
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final int CLIPBOARD_PADDING = 2;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final FontRenderer fontRenderer;
    private final String text;

    private boolean copied;

    public CopyBoxWidget(int x, int y, int width, int height, String text, FontRenderer fontRenderer) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.fontRenderer = fontRenderer;
    }

    public void draw(int mouseX, int mouseY) {
        boolean selected = mouseX >= this.x && mouseY >= this.y && mouseX <= this.x + this.width && mouseY <= this.y + this.height;
        if (!selected) {
            this.copied = false;
        }

        drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, this.copied ? 0xFF3CA03C : 0xFFA0A0A0);
        drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);

        int clipboardSize = Math.min(this.width, this.height) - CLIPBOARD_PADDING * 2;

        String text = this.fontRenderer.wrapStringToWidth(this.text, this.width - 8 - clipboardSize);

        int textX = this.x + 4;
        int textY = this.y + (this.height - 8) / 2;
        this.fontRenderer.drawWithShadow(text, textX, textY, 0xFFFFFF);

        int minClipboardX = this.x + this.width - clipboardSize - CLIPBOARD_PADDING;
        int minClipboardY = this.y + CLIPBOARD_PADDING;
        int maxClipboardX = this.x + this.width - CLIPBOARD_PADDING;
        int maxClipboardY = this.y + this.height - CLIPBOARD_PADDING;

        if (mouseX >= minClipboardX && mouseY >= minClipboardY && mouseX <= maxClipboardX && mouseY <= maxClipboardY) {
            drawRect(minClipboardX, minClipboardY, maxClipboardX, maxClipboardY, 0xFF7F7F7F);
        } else {
            drawRect(minClipboardX, minClipboardY, maxClipboardX, maxClipboardY, 0xFFA0A0A0);
        }

        CLIENT.getTextureManager().bindTexture(WIDGETS_TEXTURE);
        this.drawTexturedRect(minClipboardX, minClipboardY, 64, 0, clipboardSize, clipboardSize);

        if (this.copied) {
            List<String> lines = Lists.newArrayList(I18n.translate("gui.copy_box.copied.name"));
            GuiRenderUtils.drawTooltip(lines, (minClipboardX + maxClipboardX) / 2, (minClipboardY + maxClipboardY) / 2);

            GlStateManager.disableLighting();
            GlStateManager.enableTexture();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int clipboardWidth = Math.min(this.width, this.height) - CLIPBOARD_PADDING * 2;

        int minClipboardX = this.x + this.width - clipboardWidth - CLIPBOARD_PADDING;
        int minClipboardY = this.y + CLIPBOARD_PADDING;
        int maxClipboardX = this.x + this.width - CLIPBOARD_PADDING;
        int maxClipboardY = this.y + this.height - CLIPBOARD_PADDING;

        if (mouseX >= minClipboardX && mouseY >= minClipboardY && mouseX <= maxClipboardX && mouseY <= maxClipboardY) {
            MinecraftClient.getInstance().keyboard.setClipboard(this.text);
            this.copied = true;
            return true;
        }

        return false;
    }
}
