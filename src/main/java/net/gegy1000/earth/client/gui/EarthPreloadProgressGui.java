package net.gegy1000.earth.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EarthPreloadProgressGui extends GuiScreen {
    private static final int CANCEL_BUTTON = 0;

    private static final int BAR_HEIGHT = 14;

    private static final int BAR_BACKGROUND_COLOR = 0xFF000000;
    private static final int BAR_COLOR = 0xFF336622;

    private long count;
    private long total;

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();

        this.addButton(new GuiButton(CANCEL_BUTTON, (this.width - 150) / 2, this.height - 150, 150, 20, I18n.format("gui.cancel")));
    }

    public void update(long count, long total) {
        this.count = count;
        this.total = total;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (button.id == CANCEL_BUTTON) {
            // TODO
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.drawCenteredString(this.fontRenderer, I18n.format("gui.earth.preload.downloading"), centerX, 20, 0xFFFFFF);

        this.drawProgressBar(this.count, this.total, centerY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // TODO: duplication
    private void drawProgressBar(long count, long total, int y) {
        int barWidth = MathHelper.ceil(this.width * 0.75);

        int minX = (this.width - barWidth) / 2;
        int minY = y - BAR_HEIGHT / 2;

        int maxX = (this.width + barWidth) / 2;
        int maxY = y + BAR_HEIGHT / 2;

        Gui.drawRect(minX, minY, maxX, maxY, BAR_BACKGROUND_COLOR);

        double progress = total != 0 ? (double) count / total : 0.0;
        if (progress > 0.0) {
            int progressX = MathHelper.floor((minX + 1) + (maxX - minX - 2) * progress);
            Gui.drawRect(minX + 1, minY + 1, progressX, maxY - 1, BAR_COLOR);
        }

        int centerX = (minX + maxX) / 2;
        int centerY = (minY + maxY - this.fontRenderer.FONT_HEIGHT) / 2 + 1;

        String percentageString = String.format("%.0f%%", progress * 100.0);
        this.drawCenteredString(this.fontRenderer, percentageString, centerX, centerY, 0xA0A0A0);
    }
}
