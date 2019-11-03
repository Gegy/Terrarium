package net.gegy1000.earth.client.gui.widget.map.component;

import net.gegy1000.earth.client.gui.widget.map.SlippyMap;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface MapComponent {
    void onDrawMap(SlippyMap map, ScaledResolution resolution, SlippyMapPoint mouse);

    default void onMouseClicked(SlippyMap map, SlippyMapPoint mouse) {
    }

    default void onMouseReleased(SlippyMap map, SlippyMapPoint mouse) {
    }
}
