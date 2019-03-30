package net.gegy1000.terrarium.server.world.generator.customization.widget;

import net.gegy1000.terrarium.client.gui.widget.CycleGuiWidget;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.property.CycleEnumProperty;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyValue;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CycleWidget<T extends Enum & CycleEnumProperty> implements CustomizationWidget {
    private final PropertyKey<T> propertyKey;

    public CycleWidget(PropertyKey<T> propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiButton createWidget(GenerationSettings settings, int id, int x, int y, Runnable onPropertyChange) {
        PropertyValue<T> value = settings.getValue(this.propertyKey);
        CycleGuiWidget<T> widget = new CycleGuiWidget<>(id, x, y, this.propertyKey, value);
        widget.addListener(onPropertyChange);
        return widget;
    }
}
