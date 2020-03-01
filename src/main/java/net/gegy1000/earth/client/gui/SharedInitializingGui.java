package net.gegy1000.earth.client.gui;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.shared.SharedDataInitializers;
import net.gegy1000.earth.server.shared.SharedEarthData;
import net.gegy1000.earth.server.util.ProcessTracker;
import net.gegy1000.earth.server.util.ProgressTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class SharedInitializingGui extends GuiScreen {
    private static final int RETRY_BUTTON = 0;
    private static final int CANCEL_BUTTON = 1;

    private static final int BAR_HEIGHT = 14;

    private static final int BAR_BACKGROUND_COLOR = 0xFF000000;
    private static final int BAR_WORKING_COLOR = 0xFF336622;
    private static final int BAR_ERRORED_COLOR = 0xFF662222;

    private final GuiScreen parent;
    private final Runnable onComplete;

    private ProcessTracker process;

    private boolean errored;
    private int completeTicks;

    public SharedInitializingGui(GuiScreen parent, Runnable onComplete) {
        this.parent = parent;
        this.onComplete = onComplete;

        this.startProcess();
    }

    private void startProcess() {
        this.completeTicks = 0;
        this.errored = false;

        this.process = new ProcessTracker();
        SharedDataInitializers.initialize(this.process).thenApply(data -> {
            SharedEarthData.supply(data);
            return data;
        });
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();

        if (this.errored) {
            this.addButton(new GuiButton(RETRY_BUTTON, this.width / 2 - 154, this.height - 28, 150, 20, I18n.format("gui.earth.retry")));
            this.addButton(new GuiButton(CANCEL_BUTTON, this.width / 2 + 4, this.height - 28, 150, 20, I18n.format("gui.cancel")));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) {
            return;
        }
        switch (button.id) {
            case RETRY_BUTTON:
                this.startProcess();
                this.initGui();
                break;
            case CANCEL_BUTTON:
                this.mc.displayGuiScreen(this.parent);
                break;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (this.process.isComplete()) {
            if (this.completeTicks++ > 10) {
                this.onComplete.run();
            }
        } else if (this.process.isErrored() && !this.errored) {
            this.errored = true;
            TerrariumEarth.LOGGER.warn("Failed to prepare Terrarium for use", this.process.getException());

            this.initGui();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        String title;
        String[] description;

        if (!this.process.isErrored()) {
            title = I18n.format("gui.earth.preparing");
            description = new String[] {
                    I18n.format("gui.earth.preparing.desc.1"),
                    I18n.format("gui.earth.preparing.desc.2")
            };
        } else {
            title = I18n.format("gui.earth.preparing.errored");
            description = new String[] {
                    I18n.format("gui.earth.preparing.errored.desc.1"),
                    I18n.format("gui.earth.preparing.errored.desc.2")
            };
        }

        this.drawCenteredString(this.fontRenderer, title, centerX, 20, 0xFFFFFF);

        int descriptionY = 50;
        int descriptionLineSpacing = this.fontRenderer.FONT_HEIGHT + 2;

        for (int index = 0; index < description.length; index++) {
            String line = description[index];
            int lineY = descriptionY + index * descriptionLineSpacing;
            this.drawCenteredString(this.fontRenderer, line, centerX, lineY, 0xA0A0A0);
        }

        this.process.forEach((tracker, index) -> this.drawProgressBar(tracker, centerY + index * 34));

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawProgressBar(ProgressTracker tracker, int y) {
        double progress = tracker.getProgress();
        String description = tracker.getDescription().getFormattedText();

        int barWidth = MathHelper.ceil(this.width * 0.75);

        int minX = (this.width - barWidth) / 2;
        int minY = y - BAR_HEIGHT / 2;

        int maxX = (this.width + barWidth) / 2;
        int maxY = y + BAR_HEIGHT / 2;

        Gui.drawRect(minX, minY, maxX, maxY, BAR_BACKGROUND_COLOR);

        if (progress > 0.0) {
            int color = tracker.isErrored() ? BAR_ERRORED_COLOR : BAR_WORKING_COLOR;

            int progressX = MathHelper.floor(minX + (maxX - minX) * progress);
            Gui.drawRect(minX + 1, minY + 1, progressX - 1, maxY - 1, color);
        }

        int centerX = (minX + maxX) / 2;

        int titleY = minY - this.fontRenderer.FONT_HEIGHT - 2;
        int centerY = (minY + maxY - this.fontRenderer.FONT_HEIGHT) / 2 + 1;

        String percentageString = String.format("%.0f%%", progress * 100.0);
        this.drawCenteredString(this.fontRenderer, percentageString, centerX, centerY, 0xA0A0A0);

        this.drawCenteredString(this.fontRenderer, description, centerX, titleY, 0xA0A0A0);
    }
}
