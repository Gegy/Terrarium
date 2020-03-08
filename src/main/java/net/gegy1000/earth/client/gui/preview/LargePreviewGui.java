package net.gegy1000.earth.client.gui.preview;

import net.gegy1000.terrarium.client.gui.widget.CopyBoxWidget;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class LargePreviewGui extends GuiScreen {
    private final WorldPreview preview;
    private final GuiScreen parent;
    private final String generatorSettings;

    private PreviewRenderer renderer;
    private PreviewController controller;

    private CopyBoxWidget settingsBox;

    public LargePreviewGui(WorldPreview preview, GuiScreen parent, String generatorSettings) {
        this.preview = preview;
        this.parent = parent;
        this.generatorSettings = generatorSettings;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.addButton(new GuiButton(0, 8, this.height - 30, 150, 20, I18n.format("gui.done")));

        this.renderer = new PreviewRenderer(this, 8.0, 21.0, this.width - 16.0, this.height - 57.0);
        this.controller = new PreviewController(this.renderer, 0.4F, 1.5F);

        this.settingsBox = new CopyBoxWidget(8 + 150 + 8, this.height - 28, this.width - (8 * 2) - 150 - 8, 20, this.generatorSettings, this.fontRenderer);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        this.mc.displayGuiScreen(this.parent);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        this.controller.update();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.controller.mouseClicked(mouseX, mouseY, mouseButton);
        this.settingsBox.mouseClicked(mouseX, mouseY);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        super.mouseReleased(mouseX, mouseY, mouseButton);

        this.controller.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);

        this.controller.mouseDragged(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.controller.updateMouse(mouseX, mouseY);

        this.drawDefaultBackground();

        String title = I18n.format("options.terrarium.preview_world_title.name");
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 4, 0xFFFFFF);

        float zoom = this.controller.getZoom(partialTicks);
        float rotationX = this.controller.getRotationX(partialTicks);
        float rotationY = this.controller.getRotationY(partialTicks);
        this.renderer.render(this.preview, zoom, rotationX, rotationY);

        this.settingsBox.draw(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
        }
    }
}
