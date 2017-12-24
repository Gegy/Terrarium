package net.gegy1000.terrarium.client.gui;

import net.gegy1000.terrarium.client.preview.PreviewController;
import net.gegy1000.terrarium.client.preview.PreviewRenderer;
import net.gegy1000.terrarium.client.preview.WorldPreview;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class PreviewEarthGui extends GuiScreen {
    private final WorldPreview preview;
    private final GuiScreen parent;

    private final PreviewController controller = new PreviewController(0.4F, 1.5F);

    private PreviewRenderer renderer;

    public PreviewEarthGui(WorldPreview preview, GuiScreen parent) {
        this.preview = preview;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.addButton(new GuiButton(0, this.width / 2 - 75, this.height - 28, 150, 20, I18n.translateToLocal("gui.done")));

        this.renderer = new PreviewRenderer(this, 8.0, 21.0, this.width - 16.0, this.height - 57.0);
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

        String title = I18n.translateToLocal("options.terrarium.preview_earth_title.name");
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 4, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderer.render(this.preview, this.controller.getZoom(partialTicks), this.controller.getRotationX(partialTicks));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
        }
    }
}
