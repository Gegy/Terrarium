package net.gegy1000.terrarium.client.preview;

import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

public class PreviewController {
    private final float minZoom;
    private final float maxZoom;

    private float rotationX = 45.0F;
    private float prevRotationX = this.rotationX;

    private float zoom;
    private float prevZoom;

    private int prevMouseX;
    private boolean mouseDown;

    public PreviewController(float minZoom, float maxZoom) {
        this.minZoom = minZoom;
        this.maxZoom = maxZoom;

        this.zoom = this.prevZoom = minZoom;
    }

    public void reset() {
        this.mouseDown = false;
    }

    public void update() {
        this.prevRotationX = this.rotationX;
        this.prevZoom = this.zoom;

        int scroll = Mouse.getDWheel();
        this.zoom = MathHelper.clamp(this.zoom + scroll / 1600.0F, this.minZoom, this.maxZoom);

        if (!this.mouseDown) {
            this.rotationX += 0.25F;
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            this.mouseDown = true;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            this.mouseDown = false;
        }
    }

    public void mouseDragged(int mouseX, int mouseY, int mouseButton) {
        this.rotationX += (mouseX - this.prevMouseX) * 0.5F;
    }

    public void updateMouse(int mouseX, int mouseY) {
        this.prevMouseX = mouseX;
    }

    public float getRotationX(float partialTicks) {
        return this.prevRotationX + (this.rotationX - this.prevRotationX) * partialTicks;
    }

    public float getZoom(float partialTicks) {
        return this.prevZoom + (this.zoom - this.prevZoom) * partialTicks;
    }
}
