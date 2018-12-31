package net.gegy1000.terrarium.client.preview;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class PreviewController implements GuiEventListener {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private final PreviewRenderer renderer;

    private final float minZoom;
    private final float maxZoom;

    private float rotationX = 10.0F;
    private float prevRotationX = this.rotationX;

    private float rotationY = 45.0F;
    private float prevRotationY = this.rotationY;

    private float zoom;
    private float prevZoom;

    private boolean mouseDown;

    public PreviewController(PreviewRenderer renderer, float minZoom, float maxZoom) {
        this.renderer = renderer;
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;

        this.zoom = this.prevZoom = minZoom;
    }

    public void update() {
        this.prevRotationX = this.rotationX;
        this.prevRotationY = this.rotationY;
        this.prevZoom = this.zoom;

        if (!this.mouseDown) {
            this.rotationY -= 0.25F;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return button == 0 && this.isSelected(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.rotationY += deltaX * 0.5F;
        this.rotationX += deltaY * 0.5F;

        this.rotationX = MathHelper.clamp(this.rotationX, 10.0F, 50.0F);

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.mouseDown = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double scrollAmount) {
        double mouseX = CLIENT.mouse.getX();
        double mouseY = CLIENT.mouse.getY();
        if (this.isSelected(mouseX, mouseY)) {
            float zoomAmount = (float) (scrollAmount / 1600.0);
            this.zoom = MathHelper.clamp(this.zoom + zoomAmount, this.minZoom, this.maxZoom);
            return true;
        }
        return false;
    }

    public float getRotationX(float partialTicks) {
        return this.prevRotationX + (this.rotationX - this.prevRotationX) * partialTicks;
    }

    public float getRotationY(float partialTicks) {
        return this.prevRotationY + (this.rotationY - this.prevRotationY) * partialTicks;
    }

    public float getZoom(float partialTicks) {
        return this.prevZoom + (this.zoom - this.prevZoom) * partialTicks;
    }

    private boolean isSelected(double mouseX, double mouseY) {
        return mouseX >= this.renderer.getX() && mouseY >= this.renderer.getY()
                && mouseX <= this.renderer.getX() + this.renderer.getWidth()
                && mouseY <= this.renderer.getY() + this.renderer.getHeight();
    }
}
