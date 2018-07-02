package net.gegy1000.earth.client.gui.widget.map.component;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.earth.client.gui.widget.map.SlippyMap;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MarkerMapComponent implements MapComponent {
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(Terrarium.MODID, "textures/gui/widgets.png");

    private SlippyMapPoint marker;

    public MarkerMapComponent(SlippyMapPoint marker) {
        this.marker = marker;
    }

    public MarkerMapComponent() {
        this(null);
    }

    @Override
    public void onDrawMap(SlippyMap map, ScaledResolution resolution, int mouseX, int mouseY) {
        if (this.marker != null) {
            int scale = resolution.getScaleFactor();

            int markerX = this.marker.getX(map.getCameraZoom()) - map.getCameraX();
            int markerY = this.marker.getY(map.getCameraZoom()) - map.getCameraY();

            Minecraft.getMinecraft().getTextureManager().bindTexture(WIDGETS_TEXTURE);
            Gui.drawScaledCustomSizeModalRect(markerX - 4 * scale, markerY - 8 * scale, 0.0F, 32.0F, 16, 16, 8 * scale, 8 * scale, 256, 256);
        }
    }

    @Override
    public void onMapClicked(SlippyMap map, ScaledResolution resolution, int mouseX, int mouseY) {
        int scale = resolution.getScaleFactor();
        this.marker = new SlippyMapPoint(mouseX * scale + map.getCameraX(), mouseY * scale + map.getCameraY(), map.getCameraZoom());
    }

    public SlippyMapPoint getMarker() {
        return this.marker;
    }
}
