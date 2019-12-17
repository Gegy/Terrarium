package net.gegy1000.terrarium.server.world.generator.customization.widget;

import net.gegy1000.terrarium.client.gui.widget.ToggleGuiWidget;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyKey;
import net.gegy1000.terrarium.server.world.generator.customization.property.PropertyPair;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ToggleWidget implements CustomizationWidget {
    private final PropertyKey<Boolean> propertyKey;
    private boolean locked;

    public ToggleWidget(PropertyKey<Boolean> propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiButton createWidget(GenerationSettings settings, Runnable onPropertyChange) {
        ToggleGuiWidget widget = new ToggleGuiWidget(PropertyPair.of(this.propertyKey, settings.getValue(this.propertyKey)));
        widget.setLocked(this.locked);
        widget.addListener(onPropertyChange);
        return widget;
    }

    public CustomizationWidget locked() {
        this.locked = true;
        return this;
    }
}
