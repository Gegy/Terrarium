package net.gegy1000.earth.client.gui.widget.map.component;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.gegy1000.earth.client.gui.widget.map.SlippyMap;

@Environment(EnvType.CLIENT)
public interface MapComponent {
    void onDrawMap(SlippyMap map, double mouseX, double mouseY);

    void onMapClicked(SlippyMap map, double mouseX, double mouseY);
}
