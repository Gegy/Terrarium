package net.gegy1000.terrarium.server.world.customization.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.client.gui.widget.ToggleGuiWidget;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.customization.property.PropertyKey;
import net.minecraft.client.gui.widget.ButtonWidget;

public class ToggleWidget implements CustomizationWidget {
    private final PropertyKey<Boolean> propertyKey;
    private boolean locked;

    public ToggleWidget(PropertyKey<Boolean> propertyKey) {
        this.propertyKey = propertyKey;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public ButtonWidget createWidget(GenerationSettings settings, int id, int x, int y, Runnable onPropertyChange) {
        ToggleGuiWidget widget = new ToggleGuiWidget(id, x, y, this.propertyKey, settings.getValue(this.propertyKey));
        widget.setLocked(this.locked);
        widget.addListener(onPropertyChange);
        return widget;
    }

    public CustomizationWidget locked() {
        this.locked = true;
        return this;
    }
}
