package net.gegy1000.terrarium.server.world.generator.customization;

import net.gegy1000.terrarium.client.gui.widget.SliderGuiWidget;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SliderWidget implements CustomizationWidget {
    private final PropertyKey<Number> propertyKey;

    private final double minimum;
    private final double maximum;
    private final double step;
    private final double fineStep;

    public SliderWidget(PropertyKey<Number> propertyKey, double minimum, double maximum, double step, double fineStep) {
        this.propertyKey = propertyKey;
        this.minimum = Math.min(minimum, maximum);
        this.maximum = Math.max(maximum, minimum);
        this.step = step;
        this.fineStep = fineStep;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiButton createWidget(GenerationSettings settings, int id, int x, int y) {
        PropertyValue<Number> value = settings.getProperties().getValue(this.propertyKey);
        return new SliderGuiWidget(id, x, y, this.propertyKey, value, this.minimum, this.maximum, this.step, this.fineStep);
    }
}
