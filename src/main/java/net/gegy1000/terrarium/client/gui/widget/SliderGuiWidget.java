package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyPair;
import net.gegy1000.terrarium.server.world.generator.customization.widget.SliderScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;

@SideOnly(Side.CLIENT)
public class SliderGuiWidget extends GuiButtonExt implements TooltipRenderer {
    private final PropertyPair<Number> property;

    private final DoubleFunction<String> display;

    private final List<Runnable> listeners = new ArrayList<>();
    private final SliderScale scale;

    private final double min;
    private final double max;

    private final double step;
    private final double fineStep;

    private double position;

    private boolean mouseDown;

    private float hoverTime;

    public SliderGuiWidget(
            PropertyPair<Number> property,
            double min, double max,
            double step, double fineStep,
            DoubleFunction<String> display,
            SliderScale scale
    ) {
        super(0, 0, 0, 150, 20, "");
        this.property = property;
        this.display = display;
        this.scale = scale;

        this.min = min;
        this.max = max;
        this.step = step;
        this.fineStep = fineStep;

        this.setValue(property.value.get().doubleValue());
    }

    public void addListener(Runnable listener) {
        this.listeners.add(listener);
    }

    public void setSliderPosition(double position) {
        this.position = position;
        this.updateDisplayString(this.toValue(position));
    }

    public void setValue(double value) {
        this.position = this.toPosition(value);
        this.updateDisplayString(value);
    }

    private void updateDisplayString(double value) {
        String valueString = this.display != null ? this.display.apply(value) : String.format("%.2f", value);
        this.displayString = String.format("%s: %s", this.property.key.getLocalizedName(), valueString);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            super.drawButton(mc, mouseX, mouseY, partialTicks);

            if (super.mousePressed(mc, mouseX, mouseY)) {
                this.hoverTime += partialTicks;
            } else {
                this.hoverTime = 0;
            }
        }
    }

    @Override
    public void renderTooltip(int mouseX, int mouseY) {
        if (this.hoverTime >= 15) {
            String name = TextFormatting.BLUE + this.property.key.getLocalizedName();
            String tooltip = TextFormatting.GRAY + this.property.key.getLocalizedTooltip();
            String defaults = TextFormatting.YELLOW + I18n.format("property.terrarium.slider_range.name", this.min, this.max);
            List<String> lines = Lists.newArrayList(name, tooltip, defaults);
            if (Math.abs(this.step - this.fineStep) > 1E-6) {
                lines.add(TextFormatting.DARK_GRAY.toString() + TextFormatting.ITALIC + I18n.format("property.terrarium.slider_fine.name"));
            }
            GuiRenderUtils.drawTooltip(lines, mouseX, mouseY);
        }
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
                this.hoverTime = 0;
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
        return this.stepped((double) (mouseX - (this.x + 4)) / (double) (this.width - 8));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        if (this.mouseDown) {
            this.mouseDown = false;
            double value = this.toValue(this.position);
            if (Math.abs(this.property.value.get().doubleValue() - value) > 1e-06) {
                this.property.value.set(value);
                this.listeners.forEach(Runnable::run);
            }
        }
    }

    private double stepped(double position) {
        return this.stepped(position, GuiScreen.isShiftKeyDown() ? this.fineStep : this.step);
    }

    private double stepped(double position, double step) {
        double value = this.toValue(position);
        double stepped = value - (value % step);
        return this.toPosition(stepped);
    }

    private double toPosition(double value) {
        return this.scale.reverse(toPosition(value, this.min, this.max));
    }

    private static double toPosition(double value, double min, double max) {
        double clampedValue = MathHelper.clamp(value, min, max);
        return (clampedValue - min) / (max - min);
    }

    private double toValue(double position) {
        return toValue(this.scale.apply(position), this.min, this.max);
    }

    private static double toValue(double position, double min, double max) {
        double value = min + (max - min) * position;
        return MathHelper.clamp(value, min, max);
    }
}
