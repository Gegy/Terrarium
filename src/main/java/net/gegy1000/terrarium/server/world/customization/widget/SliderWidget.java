package net.gegy1000.terrarium.server.world.customization.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.client.gui.widget.SliderGuiWidget;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.customization.property.PropertyValue;
import net.minecraft.client.gui.widget.ButtonWidget;

public class SliderWidget implements CustomizationWidget {
    private final PropertyKey<Number> propertyKey;

    private final double minimum;
    private final double maximum;
    private final double step;
    private final double fineStep;

    private final WidgetPropertyConverter converter;

    public SliderWidget(PropertyKey<Number> propertyKey, double minimum, double maximum, double step, double fineStep, WidgetPropertyConverter converter) {
        this.propertyKey = propertyKey;

        this.minimum = Math.min(minimum, maximum);
        this.maximum = Math.max(maximum, minimum);
        this.step = step;
        this.fineStep = fineStep;

        this.converter = converter;
    }

    public SliderWidget(PropertyKey<Number> propertyKey, double minimum, double maximum, double step, double fineStep) {
        this.propertyKey = propertyKey;

        this.minimum = Math.min(minimum, maximum);
        this.maximum = Math.max(maximum, minimum);
        this.step = step;
        this.fineStep = fineStep;

        this.converter = null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ButtonWidget createWidget(GenerationSettings settings, int id, int x, int y, Runnable onPropertyChange) {
        PropertyValue<Number> value = settings.getValue(this.propertyKey);
        SliderGuiWidget widget = new SliderGuiWidget(id, x, y, this.propertyKey, value, this.minimum, this.maximum, this.step, this.fineStep, this.converter);
        widget.addListener(onPropertyChange);
        return widget;
    }
}
