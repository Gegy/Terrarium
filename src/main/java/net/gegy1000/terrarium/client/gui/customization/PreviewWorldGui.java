package net.gegy1000.terrarium.client.gui.customization;

import net.gegy1000.terrarium.client.gui.widget.CopyBoxWidget;
import net.gegy1000.terrarium.client.preview.PreviewController;
import net.gegy1000.terrarium.client.preview.PreviewRenderer;
import net.gegy1000.terrarium.client.preview.WorldPreview;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import org.lwjgl.glfw.GLFW;

public class PreviewWorldGui extends Gui {
    private final WorldPreview preview;
    private final Gui parent;
    private final String generatorSettings;

    private PreviewRenderer renderer;
    private PreviewController controller;

    private CopyBoxWidget settingsBox;

    public PreviewWorldGui(WorldPreview preview, Gui parent, String generatorSettings) {
        this.preview = preview;
        this.parent = parent;
        this.generatorSettings = generatorSettings;
    }

    @Override
    public void onInitialized() {
        this.addButton(new ButtonWidget(0, 8, this.height - 30, 150, 20, I18n.translate("gui.done")) {
            @Override
            public void onPressed(double mouseX, double mouseY) {
                PreviewWorldGui.this.client.openGui(PreviewWorldGui.this.parent);
            }
        });

        this.renderer = new PreviewRenderer(this, 8.0, 21.0, this.width - 16.0, this.height - 57.0);
        this.controller = new PreviewController(this.renderer, 0.4F, 1.5F);

        this.settingsBox = new CopyBoxWidget(8 + 150 + 8, this.height - 28, this.width - (8 * 2) - 150 - 8, 20, this.generatorSettings, this.fontRenderer);

        this.listeners.add(this.controller);
        this.listeners.add(this.settingsBox);
    }

    @Override
    public void update() {
        super.update();
        this.controller.update();
    }

    @Override
    public void draw(int mouseX, int mouseY, float delta) {
        this.drawBackground();

        String title = I18n.translate("options.terrarium.preview_world_title.name");
        this.drawStringCentered(this.fontRenderer, title, this.width / 2, 4, 0xFFFFFF);

        float zoom = this.controller.getZoom(delta);
        float rotationX = this.controller.getRotationX(delta);
        float rotationY = this.controller.getRotationY(delta);
        this.renderer.render(this.preview, zoom, rotationX, rotationY);

        this.settingsBox.draw(mouseX, mouseY);

        super.draw(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scancode, int mods) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.client.openGui(this.parent);
            return true;
        }
        return false;
    }
}
