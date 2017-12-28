package net.gegy1000.terrarium.client.gui.widget.map.component;

import net.gegy1000.terrarium.client.gui.widget.map.SlippyMap;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface MapComponent {
    void onDrawMap(SlippyMap map, ScaledResolution resolution, int mouseX, int mouseY);

    void onMapClicked(SlippyMap map, ScaledResolution resolution, int mouseX, int mouseY);
}
