package net.gegy1000.terrarium.client.gui.widget;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.client.gui.GuiRenderUtils;
import net.gegy1000.terrarium.server.world.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.customization.property.PropertyValue;
import net.gegy1000.terrarium.server.world.customization.widget.WidgetPropertyConverter;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TextFormat;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class SliderGuiWidget extends ButtonWidget implements TooltipRenderer {
    private final PropertyKey<Number> propertyKey;
    private final PropertyValue<Number> property;

    private final WidgetPropertyConverter converter;

    private final List<Runnable> listeners = new ArrayList<>();

    private final double min;
    private final double max;

    private final double step;
    private final double fineStep;

    private double position;

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
        position = MathHelper.clamp(position, 0.0, 1.0);

        double value = this.toValue(position);
        if (this.converter != null) {
            value = this.converter.toUser(value);
        }

        this.position = position;
        this.text = String.format("%s: %.2f", this.propertyKey.getLocalizedName(), value);
    }

    @Override
    public void draw(int mouseX, int mouseY, float delta) {
        super.draw(mouseX, mouseY, delta);

        if (this.visible) {
            if (super.isSelected(mouseX, mouseY)) {
                this.hoverTime += delta;
            } else {
                this.hoverTime = 0;
            }

            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

            int sliderX = this.x + (int) (this.position * (this.width - 8));
            this.drawTexturedRect(sliderX, this.y, 0, 66, 4, 20);
            this.drawTexturedRect(sliderX + 4, this.y, 196, 66, 4, 20);
        }
    }

    @Override
    public void renderTooltip(int mouseX, int mouseY) {
        if (this.hoverTime >= 15) {
            String name = TextFormat.BLUE + this.propertyKey.getLocalizedName();
            String tooltip = TextFormat.GRAY + this.propertyKey.getLocalizedTooltip();
            String defaults = TextFormat.YELLOW + I18n.translate("property.terrarium.slider_range.name", this.min, this.max);
            List<String> lines = Lists.newArrayList(name, tooltip, defaults);
            if (Math.abs(this.step - this.fineStep) > 1E-6) {
                lines.add(TextFormat.DARK_GRAY.toString() + TextFormat.ITALIC + I18n.translate("property.terrarium.slider_fine.name"));
            }
            GuiRenderUtils.drawTooltip(lines, mouseX, mouseY);
        }
    }

    @Override
    protected int getTextureId(boolean var1) {
        return 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isSelected(mouseX, mouseY)) {
            this.setSliderPosition(this.getMousePosition(mouseX));
            this.hoverTime = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.setSliderPosition(this.getMousePosition(mouseX));
        this.hoverTime = 0;
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double value = this.toValue(this.position);
        if (Math.abs(this.property.get().doubleValue() - value) > 1e-06) {
            this.property.set(value);
            for (Runnable listener : this.listeners) {
                listener.run();
            }
        }
        return true;
    }

    private double getMousePosition(double mouseX) {
        return this.step((mouseX - (this.x + 4)) / (this.width - 8));
    }

    private double step(double position) {
        return this.step(position, Gui.isShiftPressed() ? this.fineStep : this.step);
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
