package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.gegy1000.terrarium.server.world.generator.customization.widget.WidgetPropertyConverter;
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

@SideOnly(Side.CLIENT)
public class SliderGuiWidget extends GuiButtonExt implements TooltipRenderer {
    private final PropertyKey<Number> propertyKey;
    private final PropertyValue<Number> property;

    private final WidgetPropertyConverter converter;

    private final List<Runnable> listeners = new ArrayList<>();

    private final double min;
    private final double max;

    private final double step;
    private final double fineStep;

    private double position;

    private boolean mouseDown;

    private float hoverTime;

    public SliderGuiWidget(int widgetId, int x, int y, PropertyKey<Number> propertyKey, PropertyValue<Number> property, double min, double max, double step, double fineStep, WidgetPropertyConverter converter) {
        super(widgetId, x, y, 150, 20, "");
        this.propertyKey = propertyKey;
        this.property = property;
        this.converter = converter;

        this.min = min;
        this.max = max;
        this.step = step;
        this.fineStep = fineStep;

        this.setSliderPosition(this.toPosition(property.get().doubleValue()));
    }

    public SliderGuiWidget(int widgetId, int x, int y, PropertyKey<Number> propertyKey, PropertyValue<Number> property, double min, double max) {
        this(widgetId, x, y, propertyKey, property, min, max, 1.0, 0.1, null);
    }

    public void addListener(Runnable listener) {
        this.listeners.add(listener);
    }

    public void setSliderPosition(double position) {
        double value = this.toValue(position);
        if (this.converter != null) {
            value = this.converter.toUser(value);
        }

        this.position = position;
        this.displayString = String.format("%s: %.2f", this.propertyKey.getLocalizedName(), value);
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
            String name = TextFormatting.BLUE + this.propertyKey.getLocalizedName();
            String tooltip = TextFormatting.GRAY + this.propertyKey.getLocalizedTooltip();
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
        return this.step((double) (mouseX - (this.x + 4)) / (double) (this.width - 8));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        if (this.mouseDown) {
            this.mouseDown = false;
            double value = this.toValue(this.position);
            if (Math.abs(this.property.get().doubleValue() - value) > 1e-06) {
                this.property.set(value);
                for (Runnable listener : this.listeners) {
                    listener.run();
                }
            }
        }
    }

    private double step(double position) {
        return this.step(position, GuiScreen.isShiftKeyDown() ? this.fineStep : this.step);
    }

    private double step(double position, double step) {
        double value = this.min + (this.max - this.min) * position;
        double clamped = MathHelper.clamp(value, this.min, this.max);
        double stepped = clamped - (clamped % step);
        if (this.converter != null) {
            stepped = this.converter.fromUser(stepped);
        }
        return this.toPosition(stepped);
    }

    private double toPosition(double value) {
        if (this.converter != null) {
            value = this.converter.toUser(value);
        }
        double clampedValue = MathHelper.clamp(value, this.min, this.max);
        return (clampedValue - this.min) / (this.max - this.min);
    }

    private double toValue(double position) {
        double value = this.min + (this.max - this.min) * position;
        double clamped = MathHelper.clamp(value, this.min, this.max);
        if (this.converter != null) {
            return this.converter.fromUser(clamped);
        }
        return clamped;
    }
}
