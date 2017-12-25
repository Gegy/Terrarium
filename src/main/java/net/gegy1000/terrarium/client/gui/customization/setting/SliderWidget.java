package net.gegy1000.terrarium.client.gui.customization.setting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SliderWidget extends GuiButton {
    private final CustomizationValue<Double> setting;

    private final double min;
    private final double max;

    private final double step;
    private final double fineStep;

    private double position;

    private boolean mouseDown;

    public SliderWidget(int widgetId, int x, int y, CustomizationValue<Double> setting, double min, double max, double step, double fineStep) {
        super(widgetId, x, y, 150, 20, "");
        this.setting = setting;
        this.min = min;
        this.max = max;
        this.step = step;
        this.fineStep = fineStep;

        this.setSliderPosition(this.toPosition(setting.get()));
    }

    public SliderWidget(int widgetId, int x, int y, CustomizationValue<Double> setting, double min, double max) {
        this(widgetId, x, y, setting, min, max, 1.0, 0.1);
    }

    public void setSliderPosition(double position) {
        double value = this.toValue(position);
        this.position = position;
        this.displayString = String.format("%s: %.2f", this.setting.getLocalizedName(), value);
    }

    @Override
    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    @Override
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            if (this.mouseDown) {
                this.setSliderPosition(MathHelper.clamp(this.getMousePosition(mouseX), 0.0, 1.0));
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawTexturedModalRect(this.x + (int) (this.position * (this.width - 8)), this.y, 0, 66, 4, 20);
            this.drawTexturedModalRect(this.x + (int) (this.position * (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.setSliderPosition(MathHelper.clamp(this.getMousePosition(mouseX), 0.0, 1.0));
            this.mouseDown = true;
            return true;
        }
        return false;
    }

    private double getMousePosition(int mouseX) {
        return this.step((double) (mouseX - (this.x + 4)) / (double) (this.width - 8));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.mouseDown = false;
        this.setting.set(this.toValue(this.position));
    }

    private double step(double position) {
        return this.step(position, GuiScreen.isShiftKeyDown() ? this.fineStep : this.step);
    }

    private double step(double position, double step) {
        double value = this.toValue(position);
        return this.toPosition(value - (value % step));
    }

    private double toPosition(double value) {
        double clampedValue = MathHelper.clamp(value, this.min, this.max);
        return (clampedValue - this.min) / (this.max - this.min);
    }

    private double toValue(double position) {
        double value = this.min + (this.max - this.min) * position;
        return MathHelper.clamp(value, this.min, this.max);
    }
}
