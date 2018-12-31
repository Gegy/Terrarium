package net.gegy1000.terrarium.server.world.customization.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.terrarium.server.world.customization.GenerationSettings;
import net.minecraft.client.gui.widget.ButtonWidget;

public interface CustomizationWidget {
    @Environment(EnvType.CLIENT)
    ButtonWidget createWidget(GenerationSettings settings, int id, int x, int y, Runnable onPropertyChange);

    @Environment(EnvType.CLIENT)
    default ButtonWidget createWidget(GenerationSettings settings, int id, int x, int y) {
        return this.createWidget(settings, id, x, y, null);
    }
}
