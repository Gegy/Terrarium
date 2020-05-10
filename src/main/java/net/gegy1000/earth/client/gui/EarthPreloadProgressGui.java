package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.message.ModifyDownloadMessage;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EarthPreloadProgressGui extends GuiScreen {
    private static final int EXIT_BUTTON = 0;
    private static final int RUN_IN_BACKGROUND_BUTTON = 1;

    private static final int BAR_HEIGHT = 14;

    private static final int BAR_BACKGROUND_COLOR = 0xFF000000;
    private static final int BAR_COLOR = 0xFF336622;

    private long count;
    private final long total;

    private GuiButton exitButton;

    public EarthPreloadProgressGui(long count, long total) {
        this.count= count;
        this.total = total;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();

        this.exitButton = this.addButton(new GuiButton(EXIT_BUTTON, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.cancel")));
        this.addButton(new GuiButton(RUN_IN_BACKGROUND_BUTTON, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.earth.run_in_background")));
    }

    private String getExitText() {
        if (this.isComplete()) {
            return I18n.format("gui.done");
        }
        return I18n.format("gui.cancel");
    }

    private boolean isComplete() {
        return this.total > 0 && this.count >= this.total;
    }

    public void update(long count) {
        this.count = count;
        if (this.count >= this.total) {
            this.exitButton.displayString = this.getExitText();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;

        if (!this.isComplete()) {
            if (button.id == EXIT_BUTTON) {
                TerrariumEarth.NETWORK.sendToServer(new ModifyDownloadMessage(true, false));
            } else if (button.id == RUN_IN_BACKGROUND_BUTTON) {
                TerrariumEarth.NETWORK.sendToServer(new ModifyDownloadMessage(false, true));
            }
        }

        this.mc.displayGuiScreen(null);
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
