package net.gegy1000.earth.client.gui.widget.map.component;

import net.gegy1000.earth.client.gui.widget.map.SlippyMap;
import net.gegy1000.earth.client.gui.widget.map.SlippyMapPoint;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class RectMapComponent implements MapComponent {
    private SlippyMapPoint start;
    private SlippyMapPoint end;

    @Override
    public void onDrawMap(SlippyMap map, ScaledResolution resolution, SlippyMapPoint mouse) {
        if (this.start != null) {
            SlippyMapPoint end = this.end != null ? this.end : mouse;

            int zoom = map.getCameraZoom();

            int startX = this.start.getX(zoom) - map.getCameraX();
            int startY = this.start.getY(zoom) - map.getCameraY();
            int endX = end.getX(zoom) - map.getCameraX();
            int endY = end.getY(zoom) - map.getCameraY();

            int left = Math.min(startX, endX);
            int right = Math.max(startX, endX);
            int top = Math.min(startY, endY);
            int bottom = Math.max(startY, endY);

            int color = 0x502080FF;
            int borderColor = 0xFF2080FF;
            int borderWidth = 3;

            Gui.drawRect(left, top, right, bottom, color);
            Gui.drawRect(left, top, right, top + borderWidth, borderColor);
            Gui.drawRect(left, bottom - borderWidth, right, bottom, borderColor);
            Gui.drawRect(left, top, left + borderWidth, bottom, borderColor);
            Gui.drawRect(right - borderWidth, top, right, bottom, borderColor);
        }
    }

    @Override
    public void onMouseReleased(SlippyMap map, SlippyMapPoint mouse) {
        if (this.start != null && this.end != null) {
            this.start = null;
            this.end = null;
        }

        if (this.start == null) {
            this.start = mouse;
        } else {
            this.end = mouse;
        }
    }

    @Nullable
    public Rect getSelectedRect() {
        if (this.start != null && this.end != null) {
            return new Rect(
                    Math.min(this.start.getLatitude(), this.end.getLatitude()),
                    Math.min(this.start.getLongitude(), this.end.getLongitude()),
                    Math.max(this.start.getLatitude(), this.end.getLatitude()),
                    Math.max(this.start.getLongitude(), this.end.getLongitude())
            );
        }
        return null;
    }

    public static class Rect {
        public final double minLatitude;
        public final double minLongitude;
        public final double maxLatitude;
        public final double maxLongitude;

        Rect(double minLatitude, double minLongitude, double maxLatitude, double maxLongitude) {
            this.minLatitude = minLatitude;
            this.minLongitude = minLongitude;
            this.maxLatitude = maxLatitude;
            this.maxLongitude = maxLongitude;
        }
    }
}
