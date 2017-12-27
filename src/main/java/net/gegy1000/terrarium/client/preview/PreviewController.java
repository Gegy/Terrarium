package net.gegy1000.terrarium.client.preview;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class PreviewController {
    private final PreviewRenderer renderer;

    private final float minZoom;
    private final float maxZoom;

    private float rotationX = 10.0F;
    private float prevRotationX = this.rotationX;

    private float rotationY = 45.0F;
    private float prevRotationY = this.rotationY;

    private float zoom;
    private float prevZoom;

    private int prevMouseX;
    private int prevMouseY;
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
            this.rotationY += 0.25F;
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isSelected(mouseX, mouseY)) {
            this.mouseDown = true;
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            this.mouseDown = false;
        }
    }

    public void mouseDragged(int mouseX, int mouseY, int mouseButton) {
        if (this.mouseDown) {
            this.rotationY += (mouseX - this.prevMouseX) * 0.5F;
            this.rotationX += (mouseY - this.prevMouseY) * 0.5F;

            this.rotationX = MathHelper.clamp(this.rotationX, 10.0F, 50.0F);
        }
    }

    public void updateMouse(int mouseX, int mouseY) {
        this.prevMouseX = mouseX;
        this.prevMouseY = mouseY;

        int scroll = Mouse.getDWheel();
        if (this.isSelected(mouseX, mouseY)) {
            this.zoom = MathHelper.clamp(this.zoom + scroll / 1600.0F, this.minZoom, this.maxZoom);
        }
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

    private boolean isSelected(int mouseX, int mouseY) {
        return mouseX >= this.renderer.getX() && mouseY >= this.renderer.getY()
                && mouseX <= this.renderer.getX() + this.renderer.getWidth()
                && mouseY <= this.renderer.getY() + this.renderer.getHeight();
    }
}
