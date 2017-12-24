package net.gegy1000.terrarium.client.gui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.gegy1000.terrarium.client.preview.PreviewController;
import net.gegy1000.terrarium.client.preview.PreviewRenderer;
import net.gegy1000.terrarium.client.preview.WorldPreview;
import net.gegy1000.terrarium.server.world.EarthGenerationSettings;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SideOnly(Side.CLIENT)
public class CustomizeEarthGui extends GuiScreen {
    private static final int CANCEL_BUTTON = 0;
    private static final int DONE_BUTTON = 1;
    private static final int PREVIEW_BUTTON = 2;

    private static final int PADDING_X = 14;
    private static final int PADDING_Y = 36;

    private final GuiCreateWorld parent;

    private final EarthGenerationSettings settings = new EarthGenerationSettings();

    private final ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactoryBuilder().setDaemon(true).setNameFormat("terrarium-preview-%d").build());

    private final PreviewController controller = new PreviewController(0.3F, 1.0F);

    private PreviewRenderer renderer;

    private WorldPreview preview = null;

    private boolean freeze;

    public CustomizeEarthGui(GuiCreateWorld parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.controller.reset();

        int previewWidth = this.width - PADDING_X * 2;
        int previewHeight = this.height / 2 - PADDING_Y * 2;
        int previewX = PADDING_X;
        int previewY = this.height - previewHeight - PADDING_Y;
        this.renderer = new PreviewRenderer(this, previewX, previewY, previewWidth, previewHeight);

        this.buttonList.clear();
        this.addButton(new GuiButton(CANCEL_BUTTON, this.width / 2 - 155, this.height - 28, 150, 20, I18n.translateToLocal("gui.cancel")));
        this.addButton(new GuiButton(DONE_BUTTON, this.width / 2 + 5, this.height - 28, 150, 20, I18n.translateToLocal("gui.done")));
        this.addButton(new GuiButton(PREVIEW_BUTTON, previewX + previewWidth - 20, previewY, 20, 20, "..."));

        if (!this.freeze) {
            this.rebuildState();
        }

        this.freeze = false;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.enabled && button.visible) {
            switch (button.id) {
                case CANCEL_BUTTON:
                    this.mc.displayGuiScreen(this.parent);
                    break;
                case DONE_BUTTON:
                    this.parent.chunkProviderSettingsJson = this.settings.serialize();
                    this.mc.displayGuiScreen(this.parent);
                    break;
                case PREVIEW_BUTTON:
                    this.previewLarge();
                    break;
            }
        }
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
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.controller.updateMouse(mouseX, mouseY);

        this.drawDefaultBackground();

        String title = I18n.translateToLocal("options.terrarium.customize_earth_title.name");
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, 4, 0xFFFFFF);

        this.renderer.render(this.preview, this.controller.getZoom(partialTicks), this.controller.getRotationX(partialTicks));

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void rebuildState() {
        this.deletePreview();

        BufferBuilder[] builders = new BufferBuilder[8];
        for (int i = 0; i < builders.length; i++) {
            builders[i] = new BufferBuilder(0x4000);
        }
        this.preview = new WorldPreview(this.settings, this.executor, builders);
    }

    @Override
    public void onGuiClosed() {
        if (!this.freeze) {
            super.onGuiClosed();

            this.executor.shutdownNow();

            this.deletePreview();
        }
    }

    private void previewLarge() {
        if (this.preview != null) {
            this.freeze = true;
            this.mc.displayGuiScreen(new PreviewEarthGui(this.preview, this));
        }
    }

    private void deletePreview() {
        WorldPreview preview = this.preview;
        if (preview != null) {
            preview.delete();
        }
    }
}
