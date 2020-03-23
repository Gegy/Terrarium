package net.gegy1000.earth.client.gui.widget.map.component;

import net.gegy1000.earth.client.gui.widget.map.SlippyMap;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.gegy1000.terrarium.Terrarium;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MarkerMapComponent implements MapComponent {
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation(Terrarium.ID, "textures/gui/widgets.png");

    private SlippyMapPoint marker;
    private boolean canMove;

    public MarkerMapComponent(SlippyMapPoint marker) {
        this.marker = marker;
    }

    public MarkerMapComponent() {
        this(null);
    }

    public MarkerMapComponent allowMovement() {
        this.canMove = true;
        return this;
    }

    @Override
    public void onDrawMap(SlippyMap map, ScaledResolution resolution, SlippyMapPoint mouse) {
        if (this.marker != null) {
            int scale = resolution.getScaleFactor();

            int markerX = this.marker.getX(map.getCameraZoom()) - map.getCameraX();
            int markerY = this.marker.getY(map.getCameraZoom()) - map.getCameraY();

            Minecraft.getMinecraft().getTextureManager().bindTexture(WIDGETS_TEXTURE);
            Gui.drawScaledCustomSizeModalRect(markerX - 5 * scale, markerY - 10 * scale, 0.0F, 32.0F, 16, 16, 10 * scale, 10 * scale, 256, 256);
        }
    }

    @Override
    public void onMouseReleased(SlippyMap map, SlippyMapPoint mouse) {
        if (this.canMove) {
            this.marker = mouse;
        }
    }

    public void moveMarker(double latitude, double longitude) {
        this.marker = new SlippyMapPoint(latitude, longitude);
    }

    public SlippyMapPoint getMarker() {
        return this.marker;
    }
}
