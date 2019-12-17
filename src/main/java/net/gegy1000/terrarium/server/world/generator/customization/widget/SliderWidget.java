package net.gegy1000.terrarium.server.world.generator.customization.widget;

import net.gegy1000.terrarium.client.gui.widget.SliderGuiWidget;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyPair;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.DoubleFunction;

public class SliderWidget implements CustomizationWidget {
    private final PropertyKey<Number> propertyKey;

    private double minimum = 0.0;
    private double maximum = 1.0;
    private double step = 1.0;
    private double fineStep = 1.0;

    private DoubleFunction<String> display;

    private SliderScale scale = SliderScale.linear();

    public SliderWidget(PropertyKey<Number> propertyKey) {
        this.propertyKey = propertyKey;
    }

    public SliderWidget range(double minimum, double maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
        return this;
    }

    public SliderWidget step(double step) {
        return this.step(step, step);
    }

    public SliderWidget step(double step, double fineStep) {
        this.step = step;
        this.fineStep = fineStep;
        return this;
    }

    public SliderWidget display(DoubleFunction<String> display) {
        this.display = display;
        return this;
    }

    public SliderWidget scale(SliderScale scale) {
        this.scale = scale;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiButton createWidget(GenerationSettings settings, Runnable onPropertyChange) {
        SliderGuiWidget widget = new SliderGuiWidget(
                PropertyPair.of(this.propertyKey, settings.getValue(this.propertyKey)),
                this.minimum, this.maximum,
                this.step, this.fineStep,
                this.display,
                this.scale
        );
        widget.addListener(onPropertyChange);
        return widget;
    }
}
